package com.itdev.statistics;

import com.itdev.dao.entity.Result;
import com.itdev.dao.entity.StatcheckResult;
import com.itdev.dao.entity.TrueTest;
import com.itdev.enums.TestType;
import lombok.Builder;
import lombok.Data;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
public class ResultStatistics {

    private int trueTestN;

    @Builder.Default
    private Set<PairOfTest> matched = new HashSet<>();
    @Builder.Default
    private Set<TrueTest> notMatchedTrueTests = new HashSet<>();
    @Builder.Default
    private Set<StatcheckResult> notMatchedStatcheckResultsFromE = new HashSet<>();
    @Builder.Default
    private Set<StatcheckResult> notMatchedStatcheckResultsFromA = new HashSet<>();

    @Builder.Default
    private Map<TestType, Integer> typesMissedTests = new HashMap<>();

    private int successSearchTestNFromE;
    private int mistakeTestNFromE;
    private int hallucinationNFromE;
    private int successComputationDecisionErrorNFromE;
    private int mistakeOneTailedNFromE;
    @Builder.Default
    private Map<TestType, Map<TestType, Integer>> typesMistakeInTestsNFromE = new HashMap<>();

    private int successSearchTestNFromA;
    private int mistakeTestNFromA;
    private int hallucinationNFromA;
    private int successComputationDecisionErrorNFromA;
    private int mistakeOneTailedNFromA;
    @Builder.Default
    private Map<TestType, Map<TestType, Integer>> typesMistakeInTestsNFromA = new HashMap<>();

    public void calculateAll(Result result) {
        trueTestN = result.getTrueTests().size();
        searchMatchedPairs(getAllPairs(result));
        for (PairOfTest pair : matched) {
            if (pair.isAfterProc()) {
                if (isSuccessfulSearch(pair)) {
                    successSearchTestNFromA++;
                    if (pair.isErrorEqual()) successComputationDecisionErrorNFromA++;
                    if (!pair.isOneTailedEqual()) mistakeOneTailedNFromA++;
                } else {
                    mistakeTestNFromA++;
                    addTypeMistakeInTestsN(typesMistakeInTestsNFromA, pair);
                }
            } else {
                if (isSuccessfulSearch(pair)) {
                    successSearchTestNFromE++;
                    if (pair.isErrorEqual()) successComputationDecisionErrorNFromE++;
                    if (!pair.isOneTailedEqual()) mistakeOneTailedNFromE++;
                } else {
                    mistakeTestNFromE++;
                    addTypeMistakeInTestsN(typesMistakeInTestsNFromE, pair);
                }
            }
        }
        addTypeMissedTests();
        hallucinationNFromA = notMatchedStatcheckResultsFromA.size();
        hallucinationNFromE = notMatchedStatcheckResultsFromE.size();
    }

    private void addTypeMissedTests() {
        for (TrueTest res : notMatchedTrueTests) {
            TestType trueTestType = res.getType();
            if (typesMissedTests.containsKey(trueTestType)){
                Integer n = typesMissedTests.get(trueTestType);
                typesMissedTests.put(trueTestType, ++n);
            } else {
                typesMissedTests.put(trueTestType, 1);
            }
        }
    }

    private void addTypeMistakeInTestsN(Map<TestType, Map<TestType, Integer>> typeMistakeInTestsN, PairOfTest pair) {
        TestType trueTestType = pair.trueTest.getType();
        TestType statcheckResultType = pair.statcheckResult.getType();
        if (typeMistakeInTestsN.containsKey(trueTestType)){
            Map<TestType, Integer> statcheckResultTypes = typeMistakeInTestsN.get(trueTestType);
            if (statcheckResultTypes.containsKey(statcheckResultType)){
                Integer n = statcheckResultTypes.get(statcheckResultType);
                statcheckResultTypes.put(statcheckResultType, ++n);
            } else {
                statcheckResultTypes.put(statcheckResultType, 1);
            }
        } else {
            HashMap<TestType, Integer> statcheckResultTypes = new HashMap<>();
            statcheckResultTypes.put(statcheckResultType, 1);
            typeMistakeInTestsN.put(trueTestType, statcheckResultTypes);
        }
    }

    private boolean isSuccessfulSearch(PairOfTest pair) {
        boolean result = pair.isTestTypeEqual() && pair.isTestValEqual() && pair.isNumberOfDfSame();
        if (pair.trueTest.getDf1() != null) result &= pair.isDf1Equal();
        if (pair.trueTest.getDf2() != null) result &= pair.isDf2Equal();
        return result;
    }

    private void searchMatchedPairs(List<PairOfTest> all) {
        if (all.size() == 1) {
//            throw new IllegalArgumentException("Size of argument \"all\" must be greater then 0");
            return;
        }
        List<PairOfTest> notMatched = new ArrayList<>(all);
        Collections.sort(notMatched);
        while (!notMatched.isEmpty() && notMatched.get(0).getNumberOfMatches() > 0) {
            final PairOfTest pair = notMatched.get(0);
            matched.add(pair);
            List<PairOfTest> pairsToSave = notMatched.stream()
                    .filter(pairOfTest -> !pairOfTest.contains(pair.getTrueTest(), pair.isAfterProc()))
                    .filter(pairOfTest -> !pairOfTest.contains(pair.getStatcheckResult())).toList();
            notMatched.retainAll(pairsToSave);
        }
        List<PairOfTest> copy = new ArrayList<>(
                notMatched.stream()
                        .filter(pair -> pair.getTrueTest() != null)
                        .toList()
        );
        while (!copy.isEmpty()) {
            final PairOfTest pair = copy.get(0);
                notMatchedTrueTests.add(pair.getTrueTest());
            List<PairOfTest> pairsToSave = copy.stream()
                    .filter(pairOfTest -> !pairOfTest.contains(pair.getTrueTest())).toList();
            copy.retainAll(pairsToSave);
        }
        copy = new ArrayList<>(
                notMatched.stream()
                        .filter(pair -> pair.getStatcheckResult() != null)
                        .toList()
        );
        while (!copy.isEmpty()) {
            final PairOfTest pair = copy.get(0);
            if (pair.isAfterProc()) {
                notMatchedStatcheckResultsFromA.add(pair.getStatcheckResult());
            } else {
                notMatchedStatcheckResultsFromE.add(pair.getStatcheckResult());
            }
            List<PairOfTest> pairsToSave = copy.stream()
                    .filter(pairOfTest -> !pairOfTest.contains(pair.getStatcheckResult())).toList();
            copy.retainAll(pairsToSave);
        }
    }

    private List<PairOfTest> getAllPairs(Result result) {
        List<TrueTest> trueTests = result.getTrueTests();
        trueTests.add(null);
        List<StatcheckResult> statcheckResults = result.getStatcheckResults();
        statcheckResults.add(null);
        List<PairOfTest> all = new ArrayList<>(
                trueTests.size() * statcheckResults.size());
        for (TrueTest trueTest : trueTests) {
            for (StatcheckResult statcheckResult : statcheckResults) {
                PairOfTest pair = PairOfTest.builder()
                        .trueTest(trueTest)
                        .statcheckResult(statcheckResult)
                        .afterProc(statcheckResult != null ? statcheckResult.getAfterProc() : false)
                        .build();
                pair.calculateNumberOfMatches();
                all.add(pair);
            }
        }
        trueTests.remove(trueTests.get(trueTests.size() - 1));
        statcheckResults.remove(statcheckResults.get(statcheckResults.size() - 1));
        return all;
    }

    @Data
    @Builder
    private static class PairOfTest implements Comparable<PairOfTest>{

        TrueTest trueTest;
        StatcheckResult statcheckResult;
        boolean afterProc;
        int numberOfMatches;

        @Override
        public int compareTo(PairOfTest o) {
            return Integer.compare(o.numberOfMatches, this.numberOfMatches);
        }

        public boolean contains(TrueTest trueTest) {
            return this.getTrueTest() != null &&
                    this.getTrueTest().equals(trueTest);
        }

        public boolean contains(TrueTest trueTest, boolean afterProc) {
            return this.getTrueTest() != null &&
                    this.getTrueTest().equals(trueTest) &&
                    (this.statcheckResult == null || this.isAfterProc() == afterProc);
        }

        public boolean contains(StatcheckResult statcheckResult) {
            return this.getStatcheckResult() != null &&
                    this.getStatcheckResult().equals(statcheckResult);
        }

        public void calculateNumberOfMatches() {
            int numberOfMatches = 0;
            if (isTestTypeEqual()) {
                numberOfMatches++;
            }
            if (isNumberOfDfSame()) {
                if (isDf1Equal()) numberOfMatches++;
                if (isDf2Equal()) numberOfMatches++;
            }
            if (isTestValEqual()) numberOfMatches++;
            setNumberOfMatches(numberOfMatches);
        }

        public boolean isNumberOfDfSame() {
            if (trueTest == null || statcheckResult == null) return false;
            int dfQuantityTT = trueTest.getDf1() != null ? 1 : 0;
            int dfQuantitySR = statcheckResult.getDf1() != null ? 1 : 0;
            if (trueTest != null && trueTest.getDf2() != null) dfQuantityTT++;
            if (statcheckResult != null && statcheckResult.getDf2() != null) dfQuantitySR++;
            return dfQuantityTT == dfQuantitySR;
        }
        public boolean isTestTypeEqual() {
            return trueTest != null && statcheckResult != null && trueTest.getType().equals(statcheckResult.getType());
        }
        public boolean isTestValEqual() {return trueTest != null && statcheckResult != null && trueTest.getTestValue()
                .setScale(2, RoundingMode.HALF_UP).compareTo(
                        statcheckResult.getTestValue().setScale(2, RoundingMode.HALF_UP)) == 0;}
        public boolean isDf1Equal() {
            return trueTest != null && trueTest.getDf1() != null &&
                    statcheckResult != null && statcheckResult.getDf1() != null &&
                    trueTest.getDf1().equals(statcheckResult.getDf1());
        }
        public boolean isDf2Equal() {
            return trueTest != null && trueTest.getDf2() != null &&
                    statcheckResult != null && statcheckResult.getDf2() != null &&
                    trueTest.getDf2().equals(statcheckResult.getDf2());
        }
        public boolean isErrorEqual() {return trueTest.getConsistent() == !statcheckResult.getDecisionError();}
        public boolean isOneTailedEqual() {return trueTest.getOneTailed() == statcheckResult.getOneTailed();}
    }
}
