package com.itdev.statistic;

import com.itdev.enums.SubjectDomain;
import com.itdev.enums.TestType;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.*;

import static java.lang.Math.*;

public class StatTestGenerator {

    private PValueCalculator calculator;

    public StatTestGenerator() {
        this(new PValueCalculator());
    }

    public StatTestGenerator(PValueCalculator calculator) {
        this.calculator = calculator;
    }

    public List<StatTest> generateStatTests(int testQuantity) {
        ArrayList<StatTest> tests = new ArrayList<>(testQuantity);
        boolean consistent = (int) (random() * 2) == 1;
        for (int i = 0; i < testQuantity; i++) {
            TestType type = TestType.values()[(int) (random() * TestType.values().length)];
            double testVal;
            double p;
            boolean twoTailed = (int) (random() * 2) == 1;
            switch (type) {
                case Z -> {
                    testVal = getTestVal(type);
                    p = calculator.calculatePValue(type, testVal, twoTailed);
                    tests.add(new StatTest(type, twoTailed, testVal, p, consistent));
                }
                case T, R -> {
                    int df2 = getRndValByBounds(type.DF2_BOUND.getUpperBound(), type.DF2_BOUND.getLowerBound());
                    testVal = getTestVal(type, df2);
                    p = calculator.calculatePValue(type, testVal, df2, twoTailed);
                    tests.add(new StatTest(type, twoTailed, testVal, df2, p, consistent));
                }
                case CHI_2, Q -> {
                    int df1 = getRndValByBounds(type.DF1_BOUND.getUpperBound(), type.DF1_BOUND.getLowerBound());
                    testVal = getTestVal(type, df1);
                    p = calculator.calculatePValue(type, testVal, df1);
                    tests.add(new StatTest(type, false, testVal, df1, p, consistent));
                }
                case F -> {
                    int df1 = getRndValByBounds(type.DF1_BOUND.getUpperBound(), type.DF1_BOUND.getLowerBound());
                    int df2 = getRndValByBounds(type.DF2_BOUND.getUpperBound(), type.DF2_BOUND.getLowerBound());
                    testVal = getTestVal(type, df1, df2);
                    p = calculator.calculatePValue(type, testVal, df1, df2);
                    tests.add(new StatTest(type, twoTailed, testVal, df1, df2, p, consistent));
                }
            }
        }
        return tests;
    }

    private double getTestVal(TestType type) {
        if (type.equals(TestType.Z)) {
            RealDistribution distribution = new NormalDistribution();
            return distribution.sample();
        } else throw new IllegalArgumentException("Z-tests has degrees of freedom but must not have");
    }

    private double getTestVal(TestType type, int df1, int df2) {
        if (type.equals(TestType.F) || type.equals(TestType.Z)) {
            RealDistribution distribution = new FDistribution(df1, df2);
            return distribution.sample();
        } else throw new IllegalArgumentException("F-test must have two degrees of freedom");
    }

    private double getTestVal(TestType type, int df) {
        double testVal;
        RealDistribution distribution;
        switch (type){
            case T, R -> distribution = new TDistribution(df);
            case CHI_2, Q -> distribution = new ChiSquaredDistribution(df);
            case F, Z -> throw new IllegalArgumentException("Test must have one degrees of freedom");
            default -> throw new IllegalArgumentException("Unknown TestType");
        }
        testVal = distribution.sample();
        if (type.equals(TestType.R)){
            boolean negative = testVal < 0;
            double rVal = testVal * testVal / (testVal * testVal + df);
            if (negative) rVal *= -1;
            testVal = rVal;
        }
        return testVal;
    }

    private double getRndValByBounds(double upperBound, double lowerBound) {
        return random() * (upperBound - lowerBound) + lowerBound;
    }

    private int getRndValByBounds(int upperBound, int lowerBound) {
        return (int) (random() * (upperBound - lowerBound)) + lowerBound;
    }

    public int generateTestQuantity() {
        return  (int) (random() * 6);
    }

    public SubjectDomain generateSubjectDomain() {
        int numberOfDomain =  (int) (random() * 3);
        return SubjectDomain.values()[numberOfDomain];
    }

    public List<Boolean> generateEqualities(int testQuantity) {
        List<Boolean> result = new ArrayList<>(testQuantity);
        for (int i = 0; i < testQuantity; i++) {
            result.add((int)(Math.random() * 5) == 1);
        }
        return result;
    }
}
