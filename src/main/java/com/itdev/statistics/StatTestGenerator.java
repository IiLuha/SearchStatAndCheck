package com.itdev.statistics;

import com.itdev.enums.Environment;
import com.itdev.enums.SubjectDomain;
import com.itdev.enums.TestType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.apache.commons.math3.distribution.*;
import org.springframework.stereotype.Component;

import static java.lang.Math.*;

@Component
@RequiredArgsConstructor
public class StatTestGenerator {

    private final PValueCalculator pValueCalculator;

    public StatTestGenerator() {
        this(new PValueCalculator());
    }

    public List<StatTest> generateStatTests(int testQuantity) {
        ArrayList<StatTest> tests = new ArrayList<>(testQuantity);
        boolean consistent = (int) (random() * 5) != 1;
        boolean isEqualities = (int) (random() * 5) == 1;
        for (int i = 0; i < testQuantity; i++) {
            TestType type = TestType.values()[(int) (random() * TestType.values().length)];
            BigDecimal testVal;
            BigDecimal p;
            boolean twoTailed = (int) (random() * 2) == 1;
            switch (type) {
                case Z -> {
                    testVal = new BigDecimal(String.valueOf(getTestVal(type))).setScale(3, RoundingMode.HALF_UP);
                    p = new BigDecimal(String.valueOf(pValueCalculator.calculatePValue(type, testVal.doubleValue(), twoTailed))).setScale(3, RoundingMode.HALF_UP);
                    tests.add(new StatTest(type, twoTailed, testVal, p, consistent, isEqualities));
                }
                case T, R, Q -> {
                    int df2 = getRndValByBounds(type.DF2_BOUND.getUpperBound(), type.DF2_BOUND.getLowerBound());
                    testVal = new BigDecimal(String.valueOf(getTestVal(type, df2))).setScale(3, RoundingMode.HALF_UP);
                    p = new BigDecimal(String.valueOf(pValueCalculator.calculatePValue(type, testVal.doubleValue(), df2, twoTailed))).setScale(3, RoundingMode.HALF_UP);
                    tests.add(new StatTest(type, twoTailed, testVal, df2, p, consistent, isEqualities));
                }
                case CHI2 -> {
                    int df1 = getRndValByBounds(type.DF1_BOUND.getUpperBound(), type.DF1_BOUND.getLowerBound());
                    testVal = new BigDecimal(String.valueOf(getTestVal(type, df1))).setScale(3, RoundingMode.HALF_UP);
                    p = new BigDecimal(String.valueOf(pValueCalculator.calculatePValue(type, testVal.doubleValue(), df1))).setScale(3, RoundingMode.HALF_UP);
                    tests.add(new StatTest(type, false, testVal, df1, p, consistent, isEqualities));
                }
                case F -> {
                    int df1 = getRndValByBounds(type.DF1_BOUND.getUpperBound(), type.DF1_BOUND.getLowerBound());
                    int df2 = getRndValByBounds(type.DF2_BOUND.getUpperBound(), type.DF2_BOUND.getLowerBound());
                    testVal = new BigDecimal(String.valueOf(getTestVal(type, df1, df2))).setScale(3, RoundingMode.HALF_UP);
                    p = new BigDecimal(String.valueOf(pValueCalculator.calculatePValue(type, testVal.doubleValue(), df1, df2))).setScale(3, RoundingMode.HALF_UP);
                    tests.add(new StatTest(type, false, testVal, df1, df2, p, consistent, isEqualities));
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
            case CHI2, Q -> distribution = new ChiSquaredDistribution(df);
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

    private int getRndValByBounds(int upperBound, int lowerBound) {
        return (int) (random() * (upperBound - lowerBound)) + lowerBound;
    }

    public int generateTestQuantity(Environment env) {
        if (env.ordinal() < 3) return (int) (random() * 6);
        else return (int) (random() * 2 + 1) * 2;
    }

    public SubjectDomain generateSubjectDomain() {
        int numberOfDomain =  (int) (random() * 3);
        return SubjectDomain.values()[numberOfDomain];
    }

    public Environment getEnvironment() {
        Environment[] envs = Environment.values();
        return envs[(int) (random() * envs.length)];
    }
}
