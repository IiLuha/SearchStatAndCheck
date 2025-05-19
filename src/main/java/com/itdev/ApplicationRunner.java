package com.itdev;

import com.itdev.enums.Branch;
import com.itdev.http.HttpToLLM;
import com.itdev.enums.ModelLLM;
import com.itdev.parser.ResponseParser;
import com.itdev.prompt.GeneratePromptBuilder;
import com.itdev.prompt.SearchPrompt;
import com.itdev.statistic.RStatcheckCaller;
import com.itdev.statistic.RStatsCaller;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ApplicationRunner {

    private HttpToLLM http;
    private ResponseParser parser;
    private RStatcheckCaller statcheckCaller;

    public ApplicationRunner() {
        this(new HttpToLLM(), new ResponseParser(), new RStatcheckCaller());
    }

    public ApplicationRunner(HttpToLLM http, ResponseParser parser, RStatcheckCaller statcheckCaller) {
        this.http = http;
        this.parser = parser;
        this.statcheckCaller = statcheckCaller;
    }

    public static void main(String[] args) throws IOException {
        ApplicationRunner runner = new ApplicationRunner();
        ModelLLM model = ModelLLM.valueOf(args[0].toUpperCase(Locale.ROOT));
        Branch branch = Branch.valueOf(args[1].toUpperCase(Locale.ROOT));
        switch (branch) {
            case FROM_PDF -> System.out.println(runner.searchInArticle(model));
            case FROM_LLM -> {
                String excerpt = runner.generateArticle(model);
                System.out.println(excerpt);
                String answer = runner.searchInExcerpt(excerpt, model);
                System.out.println(answer);
                String[] check = runner.runStatcheck(answer);
                for (String s : check) {
                    System.out.println(s);
                }
            }
            case GENERATE -> {
                int nRuns = Integer.parseInt(args[2]);
                for (int i = 0; i < nRuns; i++) {
                    String excerpt = runner.generateArticle(model);
                    System.out.println(excerpt);
                    String answer = runner.searchInExcerpt(excerpt, model);
                    System.out.println(answer);
                    String[] check = runner.runStatcheck(answer);
                    for (String s : check) {
                        System.out.println(s);
                    }
                }
            }
        }
    }

    private String[] runStatcheck(String answer) {
        String[] mayBeTests = answer.split("\n");
        List<String> testLines = new ArrayList<>();
        for (String mayBeTest : mayBeTests) {
            if (mayBeTest.contains("=")) testLines.add(mayBeTest);
        }
        if (testLines.size() == 0) testLines.add("no tests");
        return statcheckCaller.callStatcheck(testLines);
    }

    private String searchInArticle(ModelLLM model) throws IOException {
        try (
                PDDocument document = Loader.loadPDF(new File("articles/1article.pdf"));
                FileOutputStream stream = new FileOutputStream(Path.of("output.txt").toFile())
        ) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            //System.out.println(text);
            String response = getLlmResponse(model, SearchPrompt.PROMPT_ALL + text);
            text = parser.extractGeneratedText(response, model) + "\n end";

            return "Response:\n" + text;
        }
    }

    private String searchInExcerpt(String excerpt, ModelLLM model) {
        //System.out.println(excerpt);
        String response = getLlmResponse(model, SearchPrompt.PROMPT_ALL + excerpt);
        response = parser.extractGeneratedText(response, model) + "\n end";

        return "Response search:\n" + response;
    }

    private String generateArticle(ModelLLM model) {
        GeneratePromptBuilder builder = new GeneratePromptBuilder();
        String prompt = builder.buildRandomPrompt();
        String response;
        response = getLlmResponse(model, prompt);
        response = parser.extractGeneratedText(response, model) + "\n end";

        return "Response gen:\n" + response;
    }

    private String getLlmResponse(ModelLLM model, String prompt) {
        String response;
        switch (model) {
            case DEEPSEEK -> response = http.deepseekChatCompletion(prompt);
            case LLAMA -> response = http.llamaGenerateCompletion(prompt);
            default -> response = "Unknown model";
        }
        return response;
    }
}
