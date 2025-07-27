package com.itdev.creator;

import com.itdev.dao.entity.Result;
import com.itdev.dao.entity.StatcheckResult;
import com.itdev.dao.entity.TrueTest;
import com.itdev.enums.Environment;
import com.itdev.enums.SubjectDomain;
import com.itdev.enums.TestType;
import com.itdev.statistics.StatTest;
import com.itdev.statistics.StatcheckResultDO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class ResultCreator {

    public Result createResult(SubjectDomain subjectDomain, Environment environment,
                               String genPrompt, String genAnswer, String searchAnswer,
                               List<StatTest> statTests,
                               List<StatcheckResultDO> resultsFromExcerpt,
                               List<StatcheckResultDO> resultsFromSearch) {
        Result result = new Result();
        int sizeFromExcerpt = resultsFromExcerpt.size();
        result.setValid(true);
        result.setSubjectDomain(subjectDomain);
        result.setEnvironment(environment);
        result.setTrueTests(new ArrayList<>(statTests.size()));
        result.setGenPrompt(genPrompt);
        result.setGenAnswer(genAnswer);
        result.setSearchAnswer(searchAnswer);
        result.setStatcheckResults(new ArrayList<>(sizeFromExcerpt + resultsFromSearch.size()));
        for (StatTest statTest : statTests) {
            TrueTest trueTest = createTrueTest(statTest);
            result.addTrueTest(trueTest);
        }
        rebuildResult(result, resultsFromExcerpt, resultsFromSearch);

        return result;
    }

    private StatcheckResult createStatcheckResult(StatcheckResultDO res, boolean afterProc) {
        StatcheckResult statcheckResult = new StatcheckResult();
        statcheckResult.setAfterProc(afterProc);
        statcheckResult.setSource(res.getSource());
        statcheckResult.setType(TestType.valueOf(res.getType().toUpperCase()));
        statcheckResult.setDf1(res.getDf1());
        statcheckResult.setDf2(res.getDf2());
        statcheckResult.setTestValue(res.getTestValue());
        statcheckResult.setPComparison(res.getPComparison());
        statcheckResult.setReportedP(res.getReportedP());
        statcheckResult.setComputedP(res.getComputedP());
        statcheckResult.setError(res.isError());
        statcheckResult.setDecisionError(res.isDecision_error());
        statcheckResult.setOneTailed(res.isOneTailed());
        statcheckResult.setApaFactor(res.getApaFactor());
        return statcheckResult;
    }

    private TrueTest createTrueTest(StatTest statTest) {
        TrueTest trueTest = new TrueTest();
        trueTest.setEquality(statTest.isEquality());
        trueTest.setConsistent(statTest.isConsistent());
        trueTest.setTestValue(statTest.getTestValue());
        trueTest.setPValue(statTest.getPValue());
        trueTest.setType(statTest.getType());
        trueTest.setDf1(statTest.getDf1());
        trueTest.setDf2(statTest.getDf2());
        trueTest.setOneTailed(statTest.isOneTailed());
        return trueTest;
    }

    private StatTest returnToStatTest(TrueTest trueTest) {
        return new StatTest(
                trueTest.getType(),
                trueTest.getOneTailed(),
                trueTest.getTestValue(),
                trueTest.getDf1(),
                trueTest.getDf2(),
                trueTest.getPValue(),
                trueTest.getConsistent(),
                trueTest.getEquality()
        );
    }

    public String createTable(List<StatTest> trueTests, List<StatcheckResultDO> resultsFromExcerpt,
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

    public void rebuildResult(Result result, List<StatcheckResultDO> resultsFromExcerpt, List<StatcheckResultDO> resultsFromSearch) {
        boolean afterProc;
        result.setStatcheckResults(new ArrayList<>());
        for (StatcheckResultDO res : resultsFromExcerpt) {
            afterProc = false;
            StatcheckResult statcheckResult = createStatcheckResult(res, afterProc);
            result.addStatcheckResult(statcheckResult);
        }
        for (StatcheckResultDO res : resultsFromSearch) {
            afterProc = true;
            StatcheckResult statcheckResult = createStatcheckResult(res, afterProc);
            result.addStatcheckResult(statcheckResult);
        }
        List<StatTest> statTests = result.getTrueTests().stream().map(this::returnToStatTest).toList();
        result.setResultTable(createTable(statTests, resultsFromExcerpt, resultsFromSearch));
    }
}
