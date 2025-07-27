package com.itdev;

import com.itdev.async.AsyncRunner;
import com.itdev.dao.entity.Result;
import com.itdev.enums.Branch;
import com.itdev.enums.Environment;
import com.itdev.enums.TestType;
import com.itdev.exception.StatcheckException;
import com.itdev.enums.ModelLLM;
import com.itdev.service.ResultService;
import com.itdev.statistics.ExperimentalStatistics;
import com.itdev.statistics.ResultStatistics;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@SpringBootApplication
@RequiredArgsConstructor
public class SearchStatAndCheck implements ApplicationRunner {

    private final ResultService resultService;
    private final AsyncRunner asyncRunner;

    public static void main(String[] args) {
        var run = SpringApplication.run(SearchStatAndCheck.class, args);
        run.close();
    }

    @Override
    public void run(ApplicationArguments args) throws IOException, StatcheckException {
        if (args.getNonOptionArgs().size() < 2) {
            throw new IllegalArgumentException("Необходимо указать branch и model как позиционные аргументы");
        }
        Branch branch = Branch.valueOf(args.getNonOptionArgs().get(0).toUpperCase(Locale.ROOT));
        ModelLLM model = ModelLLM.valueOf(args.getNonOptionArgs().get(1).toUpperCase(Locale.ROOT));
        switch (branch) {
            case FROM_PDF, FROM_TXT -> System.out.println(asyncRunner.runSearchAndCheck(branch, model));
            case FROM_LLM -> {
                int nRuns = Integer.parseInt(args.getNonOptionArgs().get(2));
                List<CompletableFuture<Void>> futures = new ArrayList<>(nRuns * Environment.values().length);
                for (int i = 0; i < nRuns; i++) {
                    for (Environment env : Environment.values()) {
                        futures.add(asyncRunner.runGen(model, env, i));
                    }
                }

                CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                        futures.toArray(new CompletableFuture[0])
                );

                allFutures.join();
            }
            case CALC -> {
                StringBuilder out = new StringBuilder(
                        "environmentE/A, resultN, trueTestN, " +
                                "hallN, hallTestsN, missTestN, missPGlobal, missed, SD, SEM, mistake, SD, SEM, " +
                                "hallucination, SD, SEM, error, SD, SEM, tailed, SD, SEM\n");
                List<Result> all = resultService.findAllWithLists();
                ExperimentalStatistics exStat = getExperimentalStatistics(null, model, all);
                insertLineInTable(null, exStat, out, all);
                for (Environment env : Environment.values()) {
                    List<Result> allByEnv = resultService.findAllByEnvironmentWithLists(env);
                    ExperimentalStatistics exStatByEnv = getExperimentalStatistics(env, model, allByEnv);
                    insertLineInTable(env, exStatByEnv, out, allByEnv);
                }
                out.append("\n\n");
                out.append(addTypesMissedTestsN(exStat))
                        .append(addTypesMistakeTestsN(exStat, false))
                        .append(addTypesMistakeTestsN(exStat, true));
                System.out.println(out);
            }
            case RE_CALC -> {
                List<Result> all = resultService.findAllWithLists();
                all.forEach(result -> result.setGenAnswer(result.getGenAnswer().replaceAll("\\*", "")));
                all.forEach(result -> result.setSearchAnswer(result.getSearchAnswer().replaceAll("\\*", "")));
                List<CompletableFuture<Void>> futures = new ArrayList<>(all.size());
                for (Result res : all) {
//                    List<StatcheckResultDO> resultsFromExcerpt;
//                    String excerpt = res.getGenAnswer();
//                    try {
//                        resultsFromExcerpt = statcheckCaller.callStatcheck(List.of(excerpt));
//                    } catch (StatcheckException e) {
//                        System.err.println(e.getMessage());
//                        continue;
//                    }
//                    String answer = res.getSearchAnswer();
//                    List<String> testLines = asyncRunner.getTestLines(answer);
//                    List<StatcheckResultDO> resultsFromAnswer;
//                    try {
//                        resultsFromAnswer = statcheckCaller.callStatcheck(testLines);
//                    } catch (StatcheckException e) {
//                        System.err.println(e.getMessage());
//                        continue;
//                    }
//                    resultCreator.rebuildResult(res, resultsFromExcerpt, resultsFromAnswer);
                    futures.add(asyncRunner.reCalc(res));
                }

                CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                        futures.toArray(new CompletableFuture[0])
                );

                allFutures.join();
            }
        }
    }

    private String addTypesMissedTestsN(ExperimentalStatistics exStat) {
        StringBuilder result = new StringBuilder();
        StringBuilder secondLine = new StringBuilder();
        for (TestType type : TestType.values()) {
            result.append(type.NAME).append(", ");
            secondLine.append(exStat.getTypesMissedTestsN().get(type)).append(", ");
        }
        result.deleteCharAt(result.length() - 1).deleteCharAt(result.length() - 1).append("\n");
        secondLine.deleteCharAt(secondLine.length() - 1).deleteCharAt(secondLine.length() - 1).append("\n");
        result.append(secondLine);
        return result.toString();
    }

    private String addTypesMistakeTestsN(ExperimentalStatistics exStat, boolean afterProc) {
        StringBuilder result = new StringBuilder();
        StringBuilder nextLines = new StringBuilder();
        result.append("type\\, ");
        for (TestType typeFrom : TestType.values()) {
            result.append(typeFrom.NAME).append(", ");
            nextLines.append(typeFrom.NAME).append(", ");
            for (TestType typeTo : TestType.values()) {
                Map<TestType, Integer> testTypeIntegerMap;
                if (afterProc) {
                    testTypeIntegerMap = exStat.getTypesMistakeTestsNFromA().get(typeFrom);
                } else {
                    testTypeIntegerMap = exStat.getTypesMistakeTestsNFromE().get(typeFrom);
                }
                nextLines.append((testTypeIntegerMap != null && testTypeIntegerMap.get(typeTo) != null
                                ? testTypeIntegerMap.get(typeTo)
                                : 0))
                        .append(", ");
            }
            nextLines.deleteCharAt(nextLines.length() - 1).deleteCharAt(nextLines.length() - 1).append("\n");
        }
        result.deleteCharAt(result.length() - 1).deleteCharAt(result.length() - 1).append("\n");
        result.append(nextLines);
        return result.toString();
    }

    private void insertLineInTable(Environment env, ExperimentalStatistics exStat, StringBuilder out, List<Result> all) {
        if (env == null) out.append("all_E, ");
        else out.append(env.NAME).append("_E, ");
        out.append(exStat.getResultN()).append(", ")
                .append(exStat.getTrueTestNGlobal()).append(", ")
                .append(exStat.getHallucinationNGlobalFromE()).append(", ")
                .append(exStat.getHallucinationTestsNGlobalFromE()).append(", ")
                .append(exStat.getMissedTestNGlobalFromE()).append(", ")
                .append(exStat.getMissedTestGlobalPFromE().toString()).append(", ")
                .append(exStat.getMissedTestFromE().getMean()).append(", ")
                .append(exStat.getMissedTestFromE().getStandardDeviation()).append(", ")
                .append(exStat.getMissedTestFromE().getStandardErrorMean()).append(", ")
                .append(exStat.getMistakeTestFromE().getMean()).append(", ")
                .append(exStat.getMistakeTestFromE().getStandardDeviation()).append(", ")
                .append(exStat.getMistakeTestFromE().getStandardErrorMean()).append(", ")
                .append(exStat.getHallucinationFromE().getMean()).append(", ")
                .append(exStat.getHallucinationFromE().getStandardDeviation()).append(", ")
                .append(exStat.getHallucinationFromE().getStandardErrorMean()).append(", ")
                .append(exStat.getSuccessfulComputationDecisionErrorFromE().getMean()).append(", ")
                .append(exStat.getSuccessfulComputationDecisionErrorFromE().getStandardDeviation()).append(", ")
                .append(exStat.getSuccessfulComputationDecisionErrorFromE().getStandardErrorMean()).append(", ")
                .append(exStat.getMistakeOneTailedFromE().getMean()).append(", ")
                .append(exStat.getMistakeOneTailedFromE().getStandardDeviation()).append(", ")
                .append(exStat.getMistakeOneTailedFromE().getStandardErrorMean()).append("\n");
        if (env == null) out.append("all_A, ");
        else out.append(env.NAME).append("_A, ");
        out.append(exStat.getResultN()).append(", ")
                .append(exStat.getTrueTestNGlobal()).append(", ")
                .append(exStat.getHallucinationNGlobalFromA()).append(", ")
                .append(exStat.getHallucinationTestsNGlobalFromA()).append(", ")
                .append(exStat.getMissedTestNGlobalFromA()).append(", ")
                .append(exStat.getMissedTestGlobalPFromA().toString()).append(", ")
                .append(exStat.getMissedTestFromA().getMean()).append(", ")
                .append(exStat.getMissedTestFromA().getStandardDeviation()).append(", ")
                .append(exStat.getMissedTestFromA().getStandardErrorMean()).append(", ")
                .append(exStat.getMistakeTestFromA().getMean()).append(", ")
                .append(exStat.getMistakeTestFromA().getStandardDeviation()).append(", ")
                .append(exStat.getMistakeTestFromA().getStandardErrorMean()).append(", ")
                .append(exStat.getHallucinationFromA().getMean()).append(", ")
                .append(exStat.getHallucinationFromA().getStandardDeviation()).append(", ")
                .append(exStat.getHallucinationFromA().getStandardErrorMean()).append(", ")
                .append(exStat.getSuccessfulComputationDecisionErrorFromA().getMean()).append(", ")
                .append(exStat.getSuccessfulComputationDecisionErrorFromA().getStandardDeviation()).append(", ")
                .append(exStat.getSuccessfulComputationDecisionErrorFromA().getStandardErrorMean()).append(", ")
                .append(exStat.getMistakeOneTailedFromA().getMean()).append(", ")
                .append(exStat.getMistakeOneTailedFromA().getStandardDeviation()).append(", ")
                .append(exStat.getMistakeOneTailedFromA().getStandardErrorMean()).append("\n");
    }

    private ExperimentalStatistics getExperimentalStatistics(Environment env, ModelLLM model, List<Result> all) {
        List<ResultStatistics> resultStatistics = all.stream()
                .map(result -> {
                    ResultStatistics statistics = ResultStatistics.builder().build();
                    statistics.calculateAll(result);
                    return statistics;
                }).toList();
        ExperimentalStatistics exStat = ExperimentalStatistics.builder()
                .statistics(resultStatistics)
                .modelLLM(model)
                .environment(env).build();
        exStat.calculateAll();
        return exStat;
    }

    private String getPromptsAndExcerpts() {
        StringBuilder promptsAndExcerpts = new StringBuilder();
        List<Result> all = resultService.findAllByValid(true);
        all.stream()
                .peek(res -> promptsAndExcerpts.append("ID: ").append(res.getId()).append("\n\n"))
                .peek(res -> promptsAndExcerpts.append(res.getGenPrompt()).append("\n\n"))
                .forEach(res -> promptsAndExcerpts.append(res.getGenAnswer()).append("\n\n")
                        .append("___________________________________________________________")
                        .append("\n\n"));
        return promptsAndExcerpts.toString();
    }

    private String getTables() {
        StringBuilder tables = new StringBuilder();
        List<Result> all = resultService.findAll();
        all.stream()
                .map(Result::getResultTable)
                .forEach(table -> tables.append(table).append("\n\n"));
        return tables.toString();
    }

    private void calculateStatistic() {

    }
}
