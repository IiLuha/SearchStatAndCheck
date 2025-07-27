package com.itdev.async;

import com.itdev.creator.ResultCreator;
import com.itdev.dao.entity.Result;
import com.itdev.enums.Branch;
import com.itdev.enums.Environment;
import com.itdev.enums.ModelLLM;
import com.itdev.enums.SubjectDomain;
import com.itdev.exception.StatcheckException;
import com.itdev.http.HttpToLLM;
import com.itdev.parser.ResponseParser;
import com.itdev.prompt.GeneratePromptBuilder;
import com.itdev.prompt.SearchPrompt;
import com.itdev.service.ResultService;
import com.itdev.statistics.rcaller.RStatcheckCaller;
import com.itdev.statistics.StatTest;
import com.itdev.statistics.StatTestGenerator;
import com.itdev.statistics.StatcheckResultDO;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AsyncRunner {

    private static final String PATH = "articles/1article.";

    private final ReentrantLock lock;
    private final HttpToLLM http;
    private final ResponseParser responseParser;
    private final RStatcheckCaller statcheckCaller;
    private final GeneratePromptBuilder generatePromptBuilder;
    private final StatTestGenerator statTestGenerator;
    private final ResultCreator resultCreator;
    private final ResultService resultService;

    @Async
    public CompletableFuture<Void> runGen(ModelLLM model, Environment env, int i) {
        System.out.println("Begin " + env + " " + i);
        SubjectDomain domain = statTestGenerator.generateSubjectDomain();
        int testQuantity = statTestGenerator.generateTestQuantity(env);
        List<StatTest> tests = statTestGenerator.generateStatTests(testQuantity);
        String prompt = generatePromptBuilder.buildPrompt(tests, domain, env);
        String excerpt = "";
        while (excerpt.equals("")) {
            excerpt = generateArticle(model, tests, domain, env, prompt);
        }
        if (model.equals(ModelLLM.DEEPSEEK)) excerpt = excerpt.replaceAll("\\*", "");
        System.out.println("Get excerpt " + env + " " + i);
        List<StatcheckResultDO> resultsFromExcerpt;
        try {
            resultsFromExcerpt = runStatcheck(List.of(excerpt));
            System.out.println("Get excerpt stat " + env + " " + i);
        } catch (StatcheckException e) {
            System.err.println("Get excerpt stat " + env + " " + i);
            System.err.println(e.getMessage());
            return CompletableFuture.completedFuture(null);
        }
        String answer = "";
        while (answer.equals("")) {
            answer = searchInExcerpt(excerpt, model);
        }
        if (model.equals(ModelLLM.DEEPSEEK)) answer = answer.replaceAll("\\*", "");
        System.out.println("Get answer " + env + " " + i);
        List<String> testLines = getTestLines(answer);
        List<StatcheckResultDO> resultsFromAnswer;
        try {
            resultsFromAnswer = runStatcheck(testLines);
            System.out.println("Get answer stat " + env + " " + i);
        } catch (StatcheckException e) {
            System.err.println("Get answer stat " + env + " " + i);
            System.err.println(e.getMessage());
            return CompletableFuture.completedFuture(null);
        }
        Result result = resultCreator.createResult(domain, env, prompt, excerpt, answer,
                tests, resultsFromExcerpt, resultsFromAnswer);
//        System.out.println(result);
        lock.lock();
        try {
            resultService.create(result);
        } finally {
            lock.unlock();
        }
        return CompletableFuture.completedFuture(null);
    }

    public String runSearchAndCheck(Branch branch, ModelLLM model)
            throws IOException, StatcheckException {
        String searchedTests = "";
        while (searchedTests.equals("")) {
            switch (branch) {
                case FROM_PDF -> searchedTests = searchInArticle(model);
                case FROM_TXT -> searchedTests = searchInText(model);
                default -> throw new IllegalArgumentException("The search can be performed only from PDF or TXT but brunch is " + branch.name());
            }
        }
        if (model.equals(ModelLLM.DEEPSEEK)) searchedTests = searchedTests.replaceAll("[*]", "");
        List<String> testLines = getTestLines(searchedTests);
        List<StatcheckResultDO> checkedTests = runStatcheck(testLines);
        return resultCreator.createTable(List.of(), List.of(), checkedTests);
    }

    private String searchInText(ModelLLM model) throws IOException {
        String filePath = PATH + "txt";
        Path path = Paths.get(filePath);
        try (var reader = Files.newBufferedReader(path)) {
            String text = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            String response = getLlmResponse(model, SearchPrompt.PROMPT_ALL + text);
            text = responseParser.extractGeneratedText(response, model) + "\n end";
            return text;
        }
    }

    private String searchInArticle(ModelLLM model) throws IOException {
        String path = PATH + "pdf";
        try (
                PDDocument document = Loader.loadPDF(new File(path))
        ) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            String response = getLlmResponse(model, SearchPrompt.PROMPT_ALL + text);
            text = responseParser.extractGeneratedText(response, model) + "\n end";
            return text;
        }
    }

    private String searchInExcerpt(String excerpt, ModelLLM model) {
        String response = getLlmResponse(model, SearchPrompt.PROMPT_ALL + excerpt);
        response = responseParser.extractGeneratedText(response, model);
        return response;
    }

    private String generateArticle(ModelLLM model, List<StatTest> tests, SubjectDomain domain, Environment env, String prompt) {
        String response;
        response = getLlmResponse(model, prompt);
        response = responseParser.extractGeneratedText(response, model);
        return response;
    }

    private String getLlmResponse(ModelLLM model, String prompt) {
        String response;
        switch (model) {
            case DEEPSEEK, ORDEEPSEEK -> response = http.deepseekChatCompletion(prompt);
            case LLAMA -> response = http.llamaGenerate(prompt);
            default -> response = "Unknown model";
        }
        return response;
    }

    private List<StatcheckResultDO> runStatcheck(List<String> texts) throws StatcheckException {
        return statcheckCaller.callStatcheck(texts);
    }

    public List<String> getTestLines(String text) {
        String[] mayBeTests = text.split("\n");
        List<String> testLines = new ArrayList<>();
        for (String mayBeTest : mayBeTests) {
            if (mayBeTest.contains("=")) testLines.add(mayBeTest);
        }
        if (testLines.size() == 0) testLines.add("no tests");
        return testLines;
    }

    @Async
    public CompletableFuture<Void> reCalc(Result result, int i) {
        Optional<CompletableFuture<Void>> maybeCompletedFuture = deleteStarsAndResearch(result, i);
        if (maybeCompletedFuture.isPresent()) return maybeCompletedFuture.get();
        lock.lock();
        try {
            resultService.update(result.getId(), result);
        } finally {
            lock.unlock();
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> reCalc(Result res) {
        if (res.getId() % 500 == 0) System.out.println(res.getId());
        List<StatcheckResultDO> resultsFromExcerpt;
        String excerpt = res.getGenAnswer();
        try {
            resultsFromExcerpt = statcheckCaller.callStatcheck(List.of(excerpt));
        } catch (StatcheckException e) {
            System.err.println(e.getMessage());
            return CompletableFuture.completedFuture(null);
        }
        String answer = res.getSearchAnswer();
        List<String> testLines = getTestLines(answer);
        List<StatcheckResultDO> resultsFromAnswer;
        try {
            resultsFromAnswer = statcheckCaller.callStatcheck(testLines);
        } catch (StatcheckException e) {
            System.err.println(e.getMessage());
            return CompletableFuture.completedFuture(null);
        }
        resultCreator.rebuildResult(res, resultsFromExcerpt, resultsFromAnswer);
        lock.lock();
        try {
            resultService.update(res.getId(), res);
        } finally {
            lock.unlock();
        }
        return CompletableFuture.completedFuture(null);
    }

    private Optional<CompletableFuture<Void>> deleteStarsAndResearch(Result result, int i) {
        System.out.println("Begin " + i);
        String excerpt = result.getGenAnswer().replaceAll("[*]", "");
        result.setGenAnswer(excerpt);
        System.out.println("Dane excerpt " + i);
        List<StatcheckResultDO> resultsFromExcerpt;
        try {
            resultsFromExcerpt = runStatcheck(List.of(excerpt));
            System.out.println("Dane excerpt stat " + i);
        } catch (StatcheckException e) {
            System.err.println("Dane excerpt stat " + i);
            System.err.println(e.getMessage());
            return Optional.of(CompletableFuture.completedFuture(null));
        }
        String answer = "";
        while (answer.equals("")) {
            answer = searchInExcerpt(excerpt, ModelLLM.DEEPSEEK);
        }
        answer = answer.replaceAll("[*]", "");
        result.setSearchAnswer(answer);
        System.out.println("Get answer " + i);
        List<String> testLines = getTestLines(answer);
        List<StatcheckResultDO> resultsFromAnswer;
        try {
            resultsFromAnswer = runStatcheck(testLines);
            System.out.println("Get answer stat " + i);
        } catch (StatcheckException e) {
            System.err.println("Get answer stat " + i);
            System.err.println(e.getMessage());
            return Optional.of(CompletableFuture.completedFuture(null));
        }
        resultCreator.rebuildResult(result, resultsFromExcerpt, resultsFromAnswer);
        return Optional.empty();
    }
}
