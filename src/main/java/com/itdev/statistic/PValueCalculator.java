package com.itdev.statistic;

import com.itdev.enums.TestType;

import static java.lang.Math.*;

public class PValueCalculator {

    private RStatsCaller rStatsCaller;

    public PValueCalculator() {
        this(new RStatsCaller());
    }

    public PValueCalculator(RStatsCaller rStatsCaller) {
        this.rStatsCaller = rStatsCaller;
    }

    public double calculatePValue(TestType type, double testVal, boolean twoTailed) {
        if (type.equals(TestType.Z)) {
            double p = rStatsCaller.callRPNorm(abs(testVal));
            if (twoTailed) p *= 2;
            return p;
        } else throw new IllegalArgumentException("Z-tests has degrees of freedom but must not have");
    }

    public double calculatePValue(TestType type, double testVal, int df) {
        return calculatePValue( type,  testVal,  df,  false);
    }

    public double calculatePValue(TestType type, double testVal, int df, boolean twoTailed) {
        double p;
        switch (type) {
            case T, R -> {
                double arg = -1 * abs(type.equals(TestType.R) ? r2t(testVal, df) : testVal);
                p = rStatsCaller.callRPT(arg, df);
                if (twoTailed) p *= 2;
            }
            case CHI_2, Q -> p = rStatsCaller.callRPChiSq(testVal, df);
            case F, Z -> throw new IllegalArgumentException("Test must have one degrees of freedom");
            default -> throw new IllegalArgumentException("Unknown TestType");
        }
        return p;
    }

    public double calculatePValue(TestType type, double testVal, int df1, int df2) {
        if (type.equals(TestType.F) || type.equals(TestType.Z)) {
            return rStatsCaller.callRPF(testVal, df1, df2);
        } else throw new IllegalArgumentException("F-test must have two degrees of freedom");
    }

    private double r2t(double r, int df) {
        return r / sqrt((1 - pow(r, 2) / df));
    }
}
