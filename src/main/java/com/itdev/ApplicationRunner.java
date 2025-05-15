package com.itdev;

import com.itdev.http.HttpToLLM;
import com.itdev.enums.ModelLLM;
import com.itdev.parser.ResponseParser;
import com.itdev.prompt.GeneratePromptBuilder;
import com.itdev.prompt.SearchPrompt;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static com.itdev.enums.ModelLLM.*;

public class ApplicationRunner {

    private HttpToLLM http;
    private ResponseParser parser;

    public ApplicationRunner() {
        this(new HttpToLLM(), new ResponseParser());
    }

    public ApplicationRunner(HttpToLLM http, ResponseParser parser) {
        this.http = http;
        this.parser = parser;
    }

    public static void main(String[] args) throws IOException {
        ApplicationRunner runner = new ApplicationRunner();
        boolean isArticle = false;
        ModelLLM model = LLAMA;
        if (isArticle){
            System.out.println(runner.searchInArticle(model));
        } else {
            String excerpt = runner.generateArticle(model);
            System.out.println(excerpt);
            System.out.println(runner.searchInExcerpt(excerpt, model));
        }
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

        return "Response:\n" + response;
    }

    private String generateArticle(ModelLLM model) {
        GeneratePromptBuilder builder = new GeneratePromptBuilder();
        String prompt = builder.buildRandomPrompt();
        String response;
        response = getLlmResponse(model, prompt);
        response = parser.extractGeneratedText(response, model) + "\n end";

        return "Response:\n" + response;
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
