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
import com.itdev.statistic.StatcheckResultDO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@SpringBootApplication
public class SearchStatAndCheck implements ApplicationRunner {

    private HttpToLLM http;
    private ResponseParser parser;
    private RStatcheckCaller statcheckCaller;
    private GeneratePromptBuilder builder;
    private StatTestGenerator generator;

    public SearchStatAndCheck() {
        this(new HttpToLLM(),
                new ResponseParser(),
                new RStatcheckCaller(),
                new GeneratePromptBuilder(),
                new StatTestGenerator());
    }

    public SearchStatAndCheck(HttpToLLM http,
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

    public static void main(String[] args) {
        var run = SpringApplication.run(SearchStatAndCheck.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        if (args.getNonOptionArgs().size() < 2) {
            throw new IllegalArgumentException("Необходимо указать branch и model как позиционные аргументы");
        }
        Branch branch = Branch.valueOf(args.getNonOptionArgs().get(0).toUpperCase(Locale.ROOT));
        ModelLLM model = ModelLLM.valueOf(args.getNonOptionArgs().get(1).toUpperCase(Locale.ROOT));
        switch (branch) {
            case FROM_PDF -> System.out.println(searchInArticle(model));
            case FROM_LLM -> {
                int nRuns = Integer.parseInt(args.getNonOptionArgs().get(2));
                for (int i = 0; i < nRuns; i++) {
                    runGen(model);
                }
            }
        }
    }

    private void runGen(ModelLLM model) {
        SubjectDomain domain = generator.generateSubjectDomain();
        Environment env = generator.getEnvironment();
        int testQuantity = generator.generateTestQuantity(env);
        List<StatTest> tests = generator.generateStatTests(testQuantity);
        //saveTests(tests);
        String excerpt = "";
        while (excerpt.equals("")) {
            excerpt = generateArticle(model, tests, domain, env);
        }
        System.out.println("\nResponse excerpt:\n");
        System.out.println(excerpt);
        List<StatcheckResultDO> resultsFromExcerpt = runStatcheck(List.of(excerpt));
        System.out.println("\nStatcheck:\n");
        for (StatcheckResultDO res : resultsFromExcerpt) {
            System.out.println(res);
        }
        System.out.println("_______________________________________________________\n");
        String answer = "";
        while (answer.equals("")){
            answer = searchInExcerpt(excerpt, model);
        }
        System.out.println("\nResponse answer:\n");
        System.out.println(answer);
        List<String> testLines = getTestLines(answer);
        List<StatcheckResultDO> resultsFromAnswer = runStatcheck(testLines);
        System.out.println("\nStatcheck:\n");
        for (StatcheckResultDO res : resultsFromAnswer) {
            System.out.println(res);
        }
        System.out.println("_______________________________________________________\n");
        System.out.println(compare(tests, resultsFromExcerpt, resultsFromAnswer));
    }

    private String compare(List<StatTest> trueTests, List<StatcheckResultDO> resultsFromExcerpt,
                           List<StatcheckResultDO> resultsFromAnswer) {
        StringBuilder compared = new StringBuilder();
        StringBuilder trueT = new StringBuilder();
        StringBuilder fromE = new StringBuilder();
        StringBuilder fromA = new StringBuilder();
        compared.append(String.format("%-5s|%-5s|%-10s|%-5s|%-5s|%-10s|%-10s|%-5s|%-10s|%-5s",
                "from", "type", "test", "df1", "df2", "reportedP", "computedP", "err", "decisionEr", "1tail"));
        for (StatTest trueTest : trueTests) {
            trueT.append("\ntrueT|")
                    .append(String.format("%-5s|", trueTest.getType()))
                    .append(String.format("%-10s|", trueTest.getTestValue().doubleValue()))
                    .append(String.format("%-5s|", trueTest.getDf1()))
                    .append(String.format("%-5s|", trueTest.getDf2()))
                    .append(String.format("%-10s|", "-"))
                    .append(String.format("%-10s|", trueTest.getPValue()))
                    .append(String.format("%-5s|", !trueTest.isConsistent()))
                    .append(String.format("%-10s|", "-"))
                    .append(String.format("%-5s|", trueTest.isOneTailed()));
        }
        for (StatcheckResultDO fromExcerpt : resultsFromExcerpt) {
            fromE.append("\nfromE|")
                    .append(String.format("%-5s|", fromExcerpt.getType().toUpperCase(Locale.ROOT)))
                    .append(String.format("%-10s|", fromExcerpt.getTestValue()))
                    .append(String.format("%-5s|", fromExcerpt.getDf1()))
                    .append(String.format("%-5s|", fromExcerpt.getDf2()))
                    .append(String.format("%-10s|", fromExcerpt.getReportedP()))
                    .append(String.format("%-10s|", fromExcerpt.getComputedP().doubleValue()))
                    .append(String.format("%-5s|", fromExcerpt.isError()))
                    .append(String.format("%-10s|", fromExcerpt.isDecision_error()))
                    .append(String.format("%-5s|", fromExcerpt.isOneTailed()));
        }
        for (StatcheckResultDO fromAnswer : resultsFromAnswer) {
            fromA.append("\nfromA|")
                    .append(String.format("%-5s|", fromAnswer.getType().toUpperCase(Locale.ROOT)))
                    .append(String.format("%-10s|", fromAnswer.getTestValue()))
                    .append(String.format("%-5s|", fromAnswer.getDf1()))
                    .append(String.format("%-5s|", fromAnswer.getDf2()))
                    .append(String.format("%-10s|", fromAnswer.getReportedP()))
                    .append(String.format("%-10s|", fromAnswer.getComputedP().doubleValue()))
                    .append(String.format("%-5s|", fromAnswer.isError()))
                    .append(String.format("%-10s|", fromAnswer.isDecision_error()))
                    .append(String.format("%-5s|", fromAnswer.isOneTailed()));
        }
        return compared.append(trueT).append(fromE).append(fromA).toString();
    }

    private String comparePre(List<StatTest> trueTests, List<StatcheckResultDO> resultsFromExcerpt,
                           List<StatcheckResultDO> resultsFromAnswer) {
        StringBuilder compared = new StringBuilder();
        StringBuilder trueT = new StringBuilder();
        StringBuilder fromE = new StringBuilder();
        StringBuilder fromA = new StringBuilder();
        compared.append(String.format("%-5s|%-5s|%-10s|%-5s|%-5s|%-10s|%-10s|%-5s|%-10s|%-5s",
                "from", "type", "test", "df1", "df2", "reportedP", "computedP", "err", "decisionEr", "1tail"));
        for (int i = 0; i < trueTests.size(); i++) {
            StatTest trueTest = trueTests.get(i);
            Optional<StatcheckResultDO> mayBeFromExcerpt = getResult(resultsFromExcerpt, i);
            Optional<StatcheckResultDO> mayBeFromAnswer = getResult(resultsFromAnswer, trueTest);
            trueT.append("\ntrueT|")
                    .append(String.format("%-5s|", trueTest.getType()))
                    .append(String.format("%-10s|", trueTest.getTestValue().doubleValue()))
                    .append(String.format("%-5s|", trueTest.getDf1()))
                    .append(String.format("%-5s|", trueTest.getDf2()))
                    .append(String.format("%-10s|", "-"))
                    .append(String.format("%-10s|", trueTest.getPValue()))
                    .append(String.format("%-5s|", !trueTest.isConsistent()))
                    .append(String.format("%-10s|", "-"))
                    .append(String.format("%-5s|", trueTest.isOneTailed()));
            if (mayBeFromExcerpt.isPresent()) {
                StatcheckResultDO fromExcerpt = mayBeFromExcerpt.get();
                fromE.append("\nfromE|")
                        .append(String.format("%-5s|", fromExcerpt.getType().toUpperCase(Locale.ROOT)))
                        .append(String.format("%-10s|", fromExcerpt.getTestValue()))
                        .append(String.format("%-5s|", fromExcerpt.getDf1()))
                        .append(String.format("%-5s|", fromExcerpt.getDf2()))
                        .append(String.format("%-10s|", fromExcerpt.getReportedP()))
                        .append(String.format("%-10s|", fromExcerpt.getComputedP().doubleValue()))
                        .append(String.format("%-5s|", fromExcerpt.isError()))
                        .append(String.format("%-10s|", fromExcerpt.isDecision_error()))
                        .append(String.format("%-5s|", fromExcerpt.isOneTailed()));
            }
            if (mayBeFromAnswer.isPresent()) {
                StatcheckResultDO fromAnswer = mayBeFromAnswer.get();
                fromA.append("\nfromA|")
                        .append(String.format("%-5s|", fromAnswer.getType().toUpperCase(Locale.ROOT)))
                        .append(String.format("%-10s|", fromAnswer.getTestValue()))
                        .append(String.format("%-5s|", fromAnswer.getDf1()))
                        .append(String.format("%-5s|", fromAnswer.getDf2()))
                        .append(String.format("%-10s|", fromAnswer.getReportedP()))
                        .append(String.format("%-10s|", fromAnswer.getComputedP().doubleValue()))
                        .append(String.format("%-5s|", fromAnswer.isError()))
                        .append(String.format("%-10s|", fromAnswer.isDecision_error()))
                        .append(String.format("%-5s|", fromAnswer.isOneTailed()));
            }
        }
        return compared.append(trueT).append(fromE).append(fromA).toString();
    }

    private Optional<StatcheckResultDO> getResult(List<StatcheckResultDO> results, int indTrueTest) {
        StatcheckResultDO result = null;
        int index = 0;
        while (index < results.size() &&
                !(result = results.get(index)).getSource().equals(Integer.toString(indTrueTest))
        ) {
            index++;
        }
        return Optional.ofNullable(result);
    }

    private Optional<StatcheckResultDO> getResult(List<StatcheckResultDO> results, StatTest trueTest) {
        StatcheckResultDO result = null;
        int index = 0;
        while (index < results.size() &&
                !(result = results.get(index)).getType().toUpperCase(Locale.ROOT)
                        .equals(trueTest.getType().name()) &&
                !compareTestVal(trueTest.getTestValue(), result.getTestValue())
        ) {
            index++;
        }
        return Optional.ofNullable(result);
    }

    private boolean compareTestVal(BigDecimal trueTest, BigDecimal fromAnswer) {
        BigDecimal difference = trueTest.subtract(fromAnswer).abs();
        BigDecimal threshold;

        // Определяем порог в зависимости от порядка чисел
        if (fromAnswer.abs().compareTo(new BigDecimal("0.025")) > 0) {
            threshold = new BigDecimal("0.01"); // Для больших чисел
        } else {
            threshold = new BigDecimal("0.001"); // Для маленьких чисел
        }

        return difference.compareTo(threshold) <= 0;
    }

    private List<StatcheckResultDO> runStatcheck(List<String> texts) {
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

            return text;
        }
    }

    private String searchInExcerpt(String excerpt, ModelLLM model) {
        //System.out.println(excerpt);
        System.out.println(SearchPrompt.PROMPT_ALL);
        String response = getLlmResponse(model, SearchPrompt.PROMPT_ALL + excerpt);
        response = parser.extractGeneratedText(response, model);

        return response;
    }

    private String generateArticle(ModelLLM model, List<StatTest> tests, SubjectDomain domain, Environment env) {
        String prompt = builder.buildPrompt(tests, domain, env);
        String response;
        response = getLlmResponse(model, prompt);
        response = parser.extractGeneratedText(response, model);

        return response;
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
