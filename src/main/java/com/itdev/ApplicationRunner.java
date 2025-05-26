package com.itdev;

import com.itdev.enums.Branch;
import com.itdev.enums.Environment;
import com.itdev.enums.SubjectDomain;
import com.itdev.http.HttpToLLM;
import com.itdev.enums.ModelLLM;
import com.itdev.parser.ResponseParser;
import com.itdev.prompt.GeneratePromptBuilder;
import com.itdev.prompt.SearchPrompt;
import com.itdev.statistic.RStatcheckCaller;
import com.itdev.statistic.StatTest;
import com.itdev.statistic.StatTestGenerator;
import com.itdev.statistic.StatcheckResult;
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
    private GeneratePromptBuilder builder;
    private StatTestGenerator generator;

    public ApplicationRunner() {
        this(new HttpToLLM(),
                new ResponseParser(),
                new RStatcheckCaller(),
                new GeneratePromptBuilder(),
                new StatTestGenerator());
    }

    public ApplicationRunner(HttpToLLM http,
                             ResponseParser parser,
                             RStatcheckCaller statcheckCaller,
                             GeneratePromptBuilder builder,
                             StatTestGenerator generator) {
        this.http = http;
        this.parser = parser;
        this.statcheckCaller = statcheckCaller;
        this.builder = builder;
        this.generator = generator;
    }

    public static void main(String[] args) throws IOException {
        ApplicationRunner runner = new ApplicationRunner();
        ModelLLM model = ModelLLM.valueOf(args[0].toUpperCase(Locale.ROOT));
        Branch branch = Branch.valueOf(args[1].toUpperCase(Locale.ROOT));
        switch (branch) {
            case FROM_PDF -> System.out.println(runner.searchInArticle(model));
            case FROM_LLM -> {
                int nRuns = Integer.parseInt(args[2]);
                for (int i = 0; i < nRuns; i++) {
                    runner.run(model);
                }
            }
        }
    }

    private void run(ModelLLM model) {
        SubjectDomain domain = generator.generateSubjectDomain();
        Environment env = generator.getEnvironment();
        int testQuantity = generator.generateTestQuantity(env);
        List<StatTest> tests = generator.generateStatTests(testQuantity);
        String excerpt = generateArticle(model, tests, domain, env);
        System.out.println(excerpt);
        List<StatcheckResult> resultsFromExcerpt = runStatcheck(List.of(excerpt));
        System.out.println("\nStatcheck:\n");
        System.out.println(resultsFromExcerpt);
        String answer = searchInExcerpt(excerpt, model);
        System.out.println(answer);
        List<String> testLines = getTestLines(answer);
        List<StatcheckResult> resultsFromAnswer = runStatcheck(testLines);
        System.out.println("\nStatcheck:\n");
        System.out.println(resultsFromAnswer);
        System.out.println("\n");
        System.out.println(compare(tests, resultsFromExcerpt, resultsFromAnswer));
    }

    private String compare(List<StatTest> trueTests, List<StatcheckResult> resultsFromExcerpt,
                           List<StatcheckResult> resultsFromAnswer) {
        StringBuilder compared = new StringBuilder();
        StringBuilder trueT = new StringBuilder();
        StringBuilder fromE = new StringBuilder();
        StringBuilder fromA = new StringBuilder();
        compared.append(String.format("%-5s|%-5s|%-5s|%-5s|%-10s|%-10s|%-5s|%-10s|%-5s",
                "type", "test", "df1", "df2", "reportedP", "computedP", "err", "decisionEr", "1tail"));
        for (StatTest trueTest : trueTests) {
            StatcheckResult fromExcerpt = getResult(resultsFromExcerpt, trueTest);
            StatcheckResult fromAnswer = getResult(resultsFromAnswer, trueTest);
            trueT.append(String.format("\n%-5s|", trueTest.getType()))
                    .append(String.format("%-5s|", Math.round(trueTest.getTestValue() * 100) / 100.0))
                    .append(String.format("%-5s|", trueTest.getDf1()))
                    .append(String.format("%-5s|", trueTest.getDf2()))
                    .append(String.format("%-10s|", "-"))
                    .append(String.format("%-10s|", trueTest.getPValue()))
                    .append(String.format("%-5s|", !trueTest.isConsistent()))
                    .append(String.format("%-10s|", "-"))
                    .append(String.format("%-5s|", trueTest.isOneTailed()));
            if (fromExcerpt != null) {
                fromE.append(String.format("\n%-5s|", fromExcerpt.getType().toUpperCase(Locale.ROOT)))
                        .append(String.format("%-5s|", fromExcerpt.getTestValue()))
                        .append(String.format("%-5s|", fromExcerpt.getDf1()))
                        .append(String.format("%-5s|", fromExcerpt.getDf2()))
                        .append(String.format("%-10s|", fromExcerpt.getReportedP()))
                        .append(String.format("%-10s|", Math.round(fromExcerpt.getComputedP() * 100) / 100.0))
                        .append(String.format("%-5s|", fromExcerpt.isError()))
                        .append(String.format("%-10s|", fromExcerpt.isDecision_error()))
                        .append(String.format("%-5s|", fromExcerpt.isOneTailed()));
            }
            if (fromAnswer != null) {
                fromA.append(String.format("\n%-5s|", fromAnswer.getType().toUpperCase(Locale.ROOT)))
                        .append(String.format("%-5s|", fromAnswer.getTestValue()))
                        .append(String.format("%-5s|", fromAnswer.getDf1()))
                        .append(String.format("%-5s|", fromAnswer.getDf2()))
                        .append(String.format("%-10s|", fromAnswer.getReportedP()))
                        .append(String.format("%-10s|", Math.round(fromAnswer.getComputedP() * 100) / 100.0))
                        .append(String.format("%-5s|", fromAnswer.isError()))
                        .append(String.format("%-10s|", fromAnswer.isDecision_error()))
                        .append(String.format("%-5s|", fromAnswer.isOneTailed()));
            }
        }
        return compared.append(trueT).append(fromE).append(fromA).toString();
    }

    private StatcheckResult getResult(List<StatcheckResult> results, StatTest trueTest) {
        StatcheckResult result = null;
        int index = 0;
        while (index < results.size() &&
                !(result = results.get(index)).getType().toUpperCase(Locale.ROOT)
                        .equals(trueTest.getType().name()) &&
                !compareTestVal(trueTest.getTestValue(), result.getTestValue())
        ) {
            index++;
        }
        return result;
    }

    private boolean compareTestVal(Double trueTest, double fromAnswer) {
        int tValInt = trueTest.intValue();
        int fromAnswerInt = (int) fromAnswer;
        boolean intEqual = tValInt == fromAnswerInt;
        long tDecimal = Math.round((trueTest - tValInt) * 100);
        long fromAnswerDecimal = Math.round((fromAnswer - fromAnswerInt) * 100);
        boolean decimalEqual = tDecimal == fromAnswerDecimal;

        return intEqual && decimalEqual;
    }

    private List<StatcheckResult> runStatcheck( List<String> texts) {
        return statcheckCaller.callStatcheck(texts);
    }

    private List<String> getTestLines(String text) {
        String[] mayBeTests = text.split("\n");
        List<String> testLines = new ArrayList<>();
        for (String mayBeTest : mayBeTests) {
            if (mayBeTest.contains("=")) testLines.add(mayBeTest);
        }
        if (testLines.size() == 0) testLines.add("no tests");
        return testLines;
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

    private String generateArticle(ModelLLM model, List<StatTest> tests, SubjectDomain domain, Environment env) {
        String prompt = builder.buildPrompt(tests, domain, env);
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
