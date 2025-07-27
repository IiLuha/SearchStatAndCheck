package com.itdev.statistics;

import com.itdev.enums.Environment;
import com.itdev.enums.ModelLLM;
import com.itdev.enums.TestType;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ExperimentalStatistics {

    public static final int SCALE = 16;
    public static final BigDecimal ZERO = new BigDecimal(String.valueOf(0)).setScale(SCALE, RoundingMode.HALF_UP);
    public static final BigDecimal ONE = new BigDecimal(String.valueOf(1)).setScale(SCALE, RoundingMode.HALF_UP);
    public static final BigDecimal N_ONE = new BigDecimal(String.valueOf(-1)).setScale(SCALE, RoundingMode.HALF_UP);

    private Environment environment;

    private ModelLLM modelLLM;
    private List<ResultStatistics> statistics;

    private int resultN;
    @Builder.Default
    private Map<TestType, Integer> typesMissedTestsN = new HashMap<>();
    private int trueTestNGlobal;

    private int hallucinationNGlobalFromE;
    private int hallucinationTestsNGlobalFromE;
    private int missedTestNGlobalFromE;
    private BigDecimal missedTestGlobalPFromE;
    private GlobalStatistic missedTestFromE;
    private GlobalStatistic mistakeTestFromE;
    private GlobalStatistic hallucinationFromE;
    private GlobalStatistic successfulComputationDecisionErrorFromE;
    private GlobalStatistic mistakeOneTailedFromE;
    @Builder.Default
    private Map<TestType, Map<TestType, Integer>> typesMistakeTestsNFromE = new HashMap<>();

    private int hallucinationNGlobalFromA;
    private int hallucinationTestsNGlobalFromA;
    private int missedTestNGlobalFromA;
    private BigDecimal missedTestGlobalPFromA;
    private GlobalStatistic missedTestFromA;
    private GlobalStatistic mistakeTestFromA;
    private GlobalStatistic hallucinationFromA;
    private GlobalStatistic successfulComputationDecisionErrorFromA;
    private GlobalStatistic mistakeOneTailedFromA;
    @Builder.Default
    private Map<TestType, Map<TestType, Integer>> typesMistakeTestsNFromA = new HashMap<>();

    public void calculateAll() {
        resultN = statistics.size();

        missedTestFromE = new GlobalStatistic();
        mistakeTestFromE = new GlobalStatistic();
        hallucinationFromE = new GlobalStatistic();
        successfulComputationDecisionErrorFromE = new GlobalStatistic();
        mistakeOneTailedFromE = new GlobalStatistic();
        missedTestFromA = new GlobalStatistic();
        mistakeTestFromA = new GlobalStatistic();
        hallucinationFromA = new GlobalStatistic();
        successfulComputationDecisionErrorFromA = new GlobalStatistic();
        mistakeOneTailedFromA = new GlobalStatistic();
        trueTestNGlobal = 0;
        int NResultsWithNotEmptyTrueTests = 0;

        missedTestNGlobalFromE = 0;
        hallucinationNGlobalFromE = 0;
        hallucinationTestsNGlobalFromE = 0;
        int NResultsWithNotEmptySuccessfulSearchFromE = 0;
        BigDecimal missedTestPSumFromE = ZERO;
        BigDecimal mistakeTestPSumFromE = ZERO;
        BigDecimal hallucinationTestNSumFromE = ZERO;
        BigDecimal successfulComputationDecisionErrorPSumFromE = ZERO;
        BigDecimal mistakeOneTailedPSumFromE = ZERO;

        missedTestNGlobalFromA = 0;
        hallucinationNGlobalFromA = 0;
        hallucinationTestsNGlobalFromA = 0;
        int NResultsWithNotEmptySuccessfulSearchFromA = 0;
        BigDecimal missedTestPSumFromA = ZERO;
        BigDecimal mistakeTestPSumFromA = ZERO;
        BigDecimal hallucinationTestNSumFromA = ZERO;
        BigDecimal successfulComputationDecisionErrorPSumFromA = ZERO;
        BigDecimal mistakeOneTailedPSumFromA = ZERO;
        for (ResultStatistics stat : statistics) {
            addTypeMissedTests(stat);

            int trueTestsSize = stat.getTrueTestN();
            if (trueTestsSize > 0) {
                NResultsWithNotEmptyTrueTests++;
                trueTestNGlobal += trueTestsSize;

                int successSearchTestNFromE = stat.getSuccessSearchTestNFromE();
                missedTestNGlobalFromE += trueTestsSize - successSearchTestNFromE;
                int missedTestNSumFromE = trueTestsSize - successSearchTestNFromE;
                BigDecimal divisor = new BigDecimal(String.valueOf(trueTestsSize)).setScale(SCALE, RoundingMode.HALF_UP);
                BigDecimal missedTestPFromE = new BigDecimal(String.valueOf(missedTestNSumFromE))
                        .setScale(SCALE, RoundingMode.HALF_UP)
                        .divide(divisor, RoundingMode.HALF_UP);
                missedTestPSumFromE = missedTestPSumFromE.add(missedTestPFromE);
                mistakeTestPSumFromE = mistakeTestPSumFromE.add(new BigDecimal(String.valueOf(stat.getMistakeTestNFromE())));
                int hallucinationTestsNFromE = stat.getHallucinationNFromE();
                if (hallucinationTestsNFromE > 0) hallucinationTestNSumFromE = hallucinationTestNSumFromE.add(ONE);
                if(hallucinationTestsNFromE > 0) {
                    hallucinationNGlobalFromE++;
                    hallucinationTestsNGlobalFromE += hallucinationTestsNFromE;
                }
                if (successSearchTestNFromE > 0) {
                    NResultsWithNotEmptySuccessfulSearchFromE++;
                    BigDecimal divisor1 = new BigDecimal(String.valueOf(successSearchTestNFromE))
                            .setScale(SCALE, RoundingMode.HALF_UP);
                    BigDecimal successfulComputationDecisionErrorPFromE = new BigDecimal(
                            String.valueOf(stat.getSuccessComputationDecisionErrorNFromE()))
                            .setScale(SCALE, RoundingMode.HALF_UP)
                            .divide(divisor1, RoundingMode.HALF_UP);
                    successfulComputationDecisionErrorPSumFromE = successfulComputationDecisionErrorPSumFromE
                            .add(successfulComputationDecisionErrorPFromE);
                    mistakeOneTailedPSumFromE = mistakeOneTailedPSumFromE
                            .add(new BigDecimal(String.valueOf(stat.getMistakeOneTailedNFromE())));
                }
                addTypesMistakeTests(stat.getTypesMistakeInTestsNFromE(), typesMistakeTestsNFromE);

                int successSearchTestNFromA = stat.getSuccessSearchTestNFromA();
                missedTestNGlobalFromA += trueTestsSize - successSearchTestNFromA;
                int missedTestNSumFromA = trueTestsSize - successSearchTestNFromA;
                BigDecimal missedTestPFromA = new BigDecimal(String.valueOf(missedTestNSumFromA))
                        .setScale(SCALE, RoundingMode.HALF_UP)
                        .divide(divisor, RoundingMode.HALF_UP);
                missedTestPSumFromA = missedTestPSumFromA.add(missedTestPFromA);
                mistakeTestPSumFromA = mistakeTestPSumFromA.add(new BigDecimal(String.valueOf(stat.getMistakeTestNFromA())));
                int hallucinationTestsNFromA = stat.getHallucinationNFromA();
                if (hallucinationTestsNFromA > 0) hallucinationTestNSumFromA = hallucinationTestNSumFromA.add(ONE);
                if(hallucinationTestsNFromA > 0) {
                    hallucinationNGlobalFromA++;
                    hallucinationTestsNGlobalFromA += hallucinationTestsNFromA;
                }
                if (successSearchTestNFromA > 0) {
                    NResultsWithNotEmptySuccessfulSearchFromA++;
                    BigDecimal divisor1 = new BigDecimal(String.valueOf(successSearchTestNFromA))
                            .setScale(SCALE, RoundingMode.HALF_UP);
                    BigDecimal successfulComputationDecisionErrorPFromA = new BigDecimal(
                            String.valueOf(stat.getSuccessComputationDecisionErrorNFromA()))
                            .setScale(SCALE, RoundingMode.HALF_UP)
                            .divide(divisor1, RoundingMode.HALF_UP);
                    successfulComputationDecisionErrorPSumFromA = successfulComputationDecisionErrorPSumFromA
                            .add(successfulComputationDecisionErrorPFromA);
                    mistakeOneTailedPSumFromA = mistakeOneTailedPSumFromA
                            .add(new BigDecimal(String.valueOf(stat.getMistakeOneTailedNFromA())));
                }
                addTypesMistakeTests(stat.getTypesMistakeInTestsNFromA(), typesMistakeTestsNFromA);
            }
        }
        BigDecimal resN = new BigDecimal(String.valueOf(resultN));
        BigDecimal resNTT = new BigDecimal(String.valueOf(NResultsWithNotEmptyTrueTests));
        BigDecimal resNSSE = new BigDecimal(String.valueOf(NResultsWithNotEmptySuccessfulSearchFromE));
        BigDecimal resNSSA = new BigDecimal(String.valueOf(NResultsWithNotEmptySuccessfulSearchFromA));
        BigDecimal missedTestPMeanFromE = missedTestPSumFromE.divide(resNTT, RoundingMode.HALF_UP);
        BigDecimal mistakeTestPMeanFromE = mistakeTestPSumFromE.divide(resNTT, RoundingMode.HALF_UP);
        BigDecimal hallucinationPMeanFromE = hallucinationTestNSumFromE.divide(resN, RoundingMode.HALF_UP);
        BigDecimal successfulComputationDecisionErrorPMeanFromE = N_ONE;
        BigDecimal mistakeOneTailedPMeanFromE = N_ONE;
        if (NResultsWithNotEmptySuccessfulSearchFromE > 0) {
            successfulComputationDecisionErrorPMeanFromE = successfulComputationDecisionErrorPSumFromE
                    .divide(resNSSE, RoundingMode.HALF_UP);
            mistakeOneTailedPMeanFromE = mistakeOneTailedPSumFromE.divide(resNSSE, RoundingMode.HALF_UP);
        }
        BigDecimal missedTestPMeanFromA = missedTestPSumFromA.divide(resNTT, RoundingMode.HALF_UP);
        BigDecimal mistakeTestPMeanFromA = mistakeTestPSumFromA.divide(resNTT, RoundingMode.HALF_UP);
        BigDecimal hallucinationPMeanFromA = hallucinationTestNSumFromA.divide(resN, RoundingMode.HALF_UP);
        BigDecimal successfulComputationDecisionErrorPMeanFromA = N_ONE;
        BigDecimal mistakeOneTailedPMeanFromA = N_ONE;
        if (NResultsWithNotEmptySuccessfulSearchFromA > 0) {
            successfulComputationDecisionErrorPMeanFromA = successfulComputationDecisionErrorPSumFromA
                    .divide(resNSSA, RoundingMode.HALF_UP);
            mistakeOneTailedPMeanFromA = mistakeOneTailedPSumFromA.divide(resNSSA, RoundingMode.HALF_UP);
        }
        missedTestFromE.setMean(missedTestPMeanFromE);
        mistakeTestFromE.setMean(mistakeTestPMeanFromE);
        hallucinationFromE.setMean(hallucinationPMeanFromE);
        successfulComputationDecisionErrorFromE.setMean(successfulComputationDecisionErrorPMeanFromE);
        mistakeOneTailedFromE.setMean(mistakeOneTailedPMeanFromE);
        missedTestFromA.setMean(missedTestPMeanFromA);
        mistakeTestFromA.setMean(mistakeTestPMeanFromA);
        hallucinationFromA.setMean(hallucinationPMeanFromA);
        successfulComputationDecisionErrorFromA.setMean(successfulComputationDecisionErrorPMeanFromA);
        mistakeOneTailedFromA.setMean(mistakeOneTailedPMeanFromA);

        BigDecimal missedTestPDeltaSquareSumFromE = ZERO;
        BigDecimal mistakeTestPDeltaSquareSumFromE = ZERO;
        BigDecimal hallucinationPDeltaSquareSumFromE = ZERO;
        BigDecimal successfulComputationDecisionErrorPDeltaSquareSumFromE = ZERO;
        BigDecimal mistakeOneTailedPDeltaSquareSumFromE = ZERO;
        BigDecimal missedTestPDeltaSquareSumFromA = ZERO;
        BigDecimal mistakeTestPDeltaSquareSumFromA = ZERO;
        BigDecimal hallucinationPDeltaSquareSumFromA = ZERO;
        BigDecimal successfulComputationDecisionErrorPDeltaSquareSumFromA = ZERO;
        BigDecimal mistakeOneTailedPDeltaSquareSumFromA = ZERO;

        for (ResultStatistics stat : statistics) {

            int trueTestsSize = stat.getTrueTestN();
            if (trueTestsSize > 0) {

                int successSearchTestNFromE = stat.getSuccessSearchTestNFromE();
                int missedTestNSumFromE = trueTestsSize - successSearchTestNFromE;
                BigDecimal divisor = new BigDecimal(String.valueOf(trueTestsSize)).setScale(SCALE, RoundingMode.HALF_UP);
                BigDecimal missedTestPDeltaSquareFromE = new BigDecimal(String.valueOf(missedTestNSumFromE))
                        .setScale(SCALE, RoundingMode.HALF_UP)
                        .divide(divisor, RoundingMode.HALF_UP)
                        .subtract(missedTestPMeanFromE)
                        .pow(2);
                missedTestPDeltaSquareSumFromE = missedTestPDeltaSquareSumFromE.add(missedTestPDeltaSquareFromE);
                mistakeTestPDeltaSquareSumFromE = mistakeTestPDeltaSquareSumFromE.add(
                        new BigDecimal(String.valueOf(stat.getMistakeTestNFromE()))
                                .subtract(mistakeTestPMeanFromE)
                                .pow(2));
                hallucinationPDeltaSquareSumFromE = hallucinationPDeltaSquareSumFromE
                        .add(
                                new BigDecimal(String.valueOf(stat.getHallucinationNFromE()))
                                        .subtract(hallucinationPMeanFromE)
                                        .pow(2));
                if (successSearchTestNFromE > 0) {
                    BigDecimal divisor1 = new BigDecimal(String.valueOf(successSearchTestNFromE))
                            .setScale(SCALE, RoundingMode.HALF_UP);
                    BigDecimal successfulComputationDecisionErrorPDeltaSquareFromE = new BigDecimal(
                            String.valueOf(stat.getSuccessComputationDecisionErrorNFromE()))
                            .setScale(SCALE, RoundingMode.HALF_UP)
                            .divide(divisor1, RoundingMode.HALF_UP)
                            .subtract(successfulComputationDecisionErrorPMeanFromE)
                            .pow(2);
                    successfulComputationDecisionErrorPDeltaSquareSumFromE = successfulComputationDecisionErrorPDeltaSquareSumFromE
                            .add(successfulComputationDecisionErrorPDeltaSquareFromE);
                    mistakeOneTailedPDeltaSquareSumFromE = mistakeOneTailedPDeltaSquareSumFromE
                            .add(
                                    new BigDecimal(String.valueOf(stat.getMistakeOneTailedNFromE()))
                                            .subtract(mistakeOneTailedPMeanFromE)
                                            .pow(2));
                }

                int successSearchTestNFromA = stat.getSuccessSearchTestNFromA();
                int missedTestNSumFromA = trueTestsSize - successSearchTestNFromA;
                BigDecimal missedTestPDeltaSquareFromA = new BigDecimal(String.valueOf(missedTestNSumFromA))
                        .setScale(SCALE, RoundingMode.HALF_UP)
                        .divide(divisor, RoundingMode.HALF_UP)
                        .subtract(missedTestPMeanFromA)
                        .pow(2);
                missedTestPDeltaSquareSumFromA = missedTestPDeltaSquareSumFromA.add(missedTestPDeltaSquareFromA);
                mistakeTestPDeltaSquareSumFromA = mistakeTestPDeltaSquareSumFromA.add(
                        new BigDecimal(String.valueOf(stat.getMistakeTestNFromA()))
                                .subtract(mistakeTestPMeanFromA)
                                .pow(2));
                hallucinationPDeltaSquareSumFromA = hallucinationPDeltaSquareSumFromA
                        .add(
                                new BigDecimal(String.valueOf(stat.getHallucinationNFromA()))
                                        .subtract(hallucinationPMeanFromA)
                                        .pow(2));
                if (successSearchTestNFromA > 0) {
                    BigDecimal divisor1 = new BigDecimal(String.valueOf(successSearchTestNFromA)).setScale(SCALE, RoundingMode.HALF_UP);
                    BigDecimal successfulComputationDecisionErrorPDeltaSquareFromA = new BigDecimal(
                            String.valueOf(stat.getSuccessComputationDecisionErrorNFromA()))
                            .setScale(SCALE, RoundingMode.HALF_UP)
                            .divide(divisor1, RoundingMode.HALF_UP)
                            .subtract(successfulComputationDecisionErrorPMeanFromA)
                            .pow(2);
                    successfulComputationDecisionErrorPDeltaSquareSumFromA = successfulComputationDecisionErrorPDeltaSquareSumFromA
                            .add(successfulComputationDecisionErrorPDeltaSquareFromA);
                    mistakeOneTailedPDeltaSquareSumFromA = mistakeOneTailedPDeltaSquareSumFromA
                            .add(
                                    new BigDecimal(String.valueOf(stat.getMistakeOneTailedNFromA()))
                                            .subtract(mistakeOneTailedPMeanFromA)
                                            .pow(2));
                }
            }
        }

        missedTestFromE.setStandardDeviation(missedTestPDeltaSquareSumFromE
                .divide(resNTT.subtract(ONE), RoundingMode.HALF_UP)
                .sqrt(new MathContext(SCALE / 2)));
        mistakeTestFromE.setStandardDeviation(mistakeTestPDeltaSquareSumFromE
                .divide(resNTT.subtract(ONE), RoundingMode.HALF_UP)
                .sqrt(new MathContext(SCALE / 2)));
        hallucinationFromE.setStandardDeviation(hallucinationPDeltaSquareSumFromE
                .divide(resN.subtract(ONE), RoundingMode.HALF_UP)
                .sqrt(new MathContext(SCALE / 2)));
        successfulComputationDecisionErrorFromE.setStandardDeviation(successfulComputationDecisionErrorPDeltaSquareSumFromE
                .divide(resNSSE.subtract(ONE), RoundingMode.HALF_UP)
                .sqrt(new MathContext(SCALE / 2)));
        mistakeOneTailedFromE.setStandardDeviation(mistakeOneTailedPDeltaSquareSumFromE
                .divide(resNSSE.subtract(ONE), RoundingMode.HALF_UP)
                .sqrt(new MathContext(SCALE / 2)));
        missedTestFromA.setStandardDeviation(missedTestPDeltaSquareSumFromA
                .divide(resNTT.subtract(ONE), RoundingMode.HALF_UP)
                .sqrt(new MathContext(SCALE / 2)));
        mistakeTestFromA.setStandardDeviation(mistakeTestPDeltaSquareSumFromA
                .divide(resNTT.subtract(ONE), RoundingMode.HALF_UP)
                .sqrt(new MathContext(SCALE / 2)));
        hallucinationFromA.setStandardDeviation(hallucinationPDeltaSquareSumFromA
                .divide(resN.subtract(ONE), RoundingMode.HALF_UP)
                .sqrt(new MathContext(SCALE / 2)));
        successfulComputationDecisionErrorFromA.setStandardDeviation(successfulComputationDecisionErrorPDeltaSquareSumFromA
                .divide(resNSSA.subtract(ONE), RoundingMode.HALF_UP)
                .sqrt(new MathContext(SCALE / 2)));
        mistakeOneTailedFromA.setStandardDeviation(mistakeOneTailedPDeltaSquareSumFromA
                .divide(resNSSA.subtract(ONE), RoundingMode.HALF_UP)
                .sqrt(new MathContext(SCALE / 2)));

        missedTestFromE.setStandardErrorMean(missedTestPDeltaSquareSumFromE
                .divide(resNTT.subtract(ONE), RoundingMode.HALF_UP)
                .divide(resNTT, RoundingMode.HALF_UP)
                .sqrt(new MathContext(SCALE / 2)));
        mistakeTestFromE.setStandardErrorMean(mistakeTestPDeltaSquareSumFromE
                .divide(resNTT.subtract(ONE), RoundingMode.HALF_UP)
                .divide(resNTT, RoundingMode.HALF_UP)
                .sqrt(new MathContext(SCALE / 2)));
        hallucinationFromE.setStandardErrorMean(hallucinationPDeltaSquareSumFromE
                .divide(resN.subtract(ONE), RoundingMode.HALF_UP)
                .divide(resN, RoundingMode.HALF_UP)
                .sqrt(new MathContext(SCALE / 2)));
        if (NResultsWithNotEmptySuccessfulSearchFromE > 0) {
            successfulComputationDecisionErrorFromE.setStandardErrorMean(successfulComputationDecisionErrorPDeltaSquareSumFromE
                    .divide(resNSSE.subtract(ONE), RoundingMode.HALF_UP)
                    .divide(resNSSE, RoundingMode.HALF_UP)
                    .sqrt(new MathContext(SCALE / 2)));
            mistakeOneTailedFromE.setStandardErrorMean(mistakeOneTailedPDeltaSquareSumFromE
                    .divide(resNSSE.subtract(ONE), RoundingMode.HALF_UP)
                    .divide(resNSSE, RoundingMode.HALF_UP)
                    .sqrt(new MathContext(SCALE / 2)));
        } else {
            successfulComputationDecisionErrorFromE.setStandardErrorMean(successfulComputationDecisionErrorPDeltaSquareSumFromE);
            mistakeOneTailedFromE.setStandardErrorMean(mistakeOneTailedPDeltaSquareSumFromE);
        }
        missedTestFromA.setStandardErrorMean(missedTestPDeltaSquareSumFromA
                .divide(resNTT.subtract(ONE), RoundingMode.HALF_UP)
                .divide(resNTT, RoundingMode.HALF_UP)
                .sqrt(new MathContext(SCALE / 2)));
        mistakeTestFromA.setStandardErrorMean(mistakeTestPDeltaSquareSumFromA
                .divide(resNTT.subtract(ONE), RoundingMode.HALF_UP)
                .divide(resNTT, RoundingMode.HALF_UP)
                .sqrt(new MathContext(SCALE / 2)));
        hallucinationFromA.setStandardErrorMean(hallucinationPDeltaSquareSumFromA
                .divide(resN.subtract(ONE), RoundingMode.HALF_UP)
                .divide(resN, RoundingMode.HALF_UP)
                .sqrt(new MathContext(SCALE / 2)));
        if (NResultsWithNotEmptySuccessfulSearchFromA > 0) {
            successfulComputationDecisionErrorFromA.setStandardErrorMean(successfulComputationDecisionErrorPDeltaSquareSumFromA
                    .divide(resNSSA.subtract(ONE), RoundingMode.HALF_UP)
                    .divide(resNSSA, RoundingMode.HALF_UP)
                    .sqrt(new MathContext(SCALE / 2)));
            mistakeOneTailedFromA.setStandardErrorMean(mistakeOneTailedPDeltaSquareSumFromA
                    .divide(resNSSA.subtract(ONE), RoundingMode.HALF_UP)
                    .divide(resNSSA, RoundingMode.HALF_UP)
                    .sqrt(new MathContext(SCALE / 2)));
        } else {
            successfulComputationDecisionErrorFromA.setStandardErrorMean(successfulComputationDecisionErrorPDeltaSquareSumFromA);
            mistakeOneTailedFromA.setStandardErrorMean(mistakeOneTailedPDeltaSquareSumFromA);
        }

        missedTestGlobalPFromE = new BigDecimal(String.valueOf(missedTestNGlobalFromE))
                .setScale(SCALE, RoundingMode.HALF_UP)
                .divide(new BigDecimal(String.valueOf(trueTestNGlobal))
                        .setScale(SCALE, RoundingMode.HALF_UP), RoundingMode.HALF_UP);

        missedTestGlobalPFromA = new BigDecimal(String.valueOf(missedTestNGlobalFromA))
                .setScale(SCALE, RoundingMode.HALF_UP)
                .divide(new BigDecimal(String.valueOf(trueTestNGlobal))
                        .setScale(SCALE, RoundingMode.HALF_UP), RoundingMode.HALF_UP);
    }

    private void addTypesMistakeTests(Map<TestType, Map<TestType, Integer>> typesMistakeTestsNFrom,
                                      Map<TestType, Map<TestType, Integer>> typesMistakeTestsNTo) {
        for (Map.Entry<TestType, Map<TestType, Integer>> entryFrom : typesMistakeTestsNFrom.entrySet()) {
            TestType trueTestTypeFrom = entryFrom.getKey();
            for (Map.Entry<TestType, Integer> entryTo : entryFrom.getValue().entrySet()) {
                TestType trueTestTypeTo = entryTo.getKey();
                if (typesMistakeTestsNTo.containsKey(trueTestTypeFrom)) {
                    Map<TestType, Integer> typesMistakeTestsNToVal = typesMistakeTestsNTo.get(trueTestTypeFrom);
                    if (typesMistakeTestsNToVal.containsKey(trueTestTypeTo)) {
                        Integer n = typesMistakeTestsNToVal.get(trueTestTypeTo);
                        typesMistakeTestsNToVal.put(trueTestTypeTo,
                                n + entryTo.getValue());
                    } else {
                        typesMistakeTestsNToVal.put(trueTestTypeTo, 1);
                    }
                } else {
                    HashMap<TestType, Integer> typesMistakeTestsNToVal = new HashMap<>();
                    typesMistakeTestsNToVal.put(trueTestTypeTo, 1);
                    typesMistakeTestsNTo.put(trueTestTypeFrom, typesMistakeTestsNToVal);
                }
            }
        }
    }

    private void addTypeMissedTests(ResultStatistics stat) {
        Map<TestType, Integer> typesMissedTests = stat.getTypesMissedTests();
        for (TestType trueTestType : typesMissedTests.keySet()) {
            if (typesMissedTestsN.containsKey(trueTestType)) {
                Integer n = typesMissedTestsN.get(trueTestType);
                typesMissedTestsN.put(trueTestType, n + typesMissedTests.get(trueTestType));
            } else {
                typesMissedTestsN.put(trueTestType, 1);
            }
        }
    }

    @Data
    @NoArgsConstructor
    public static class GlobalStatistic {

        BigDecimal mean;
        BigDecimal standardDeviation;
        BigDecimal standardErrorMean;
    }
}
