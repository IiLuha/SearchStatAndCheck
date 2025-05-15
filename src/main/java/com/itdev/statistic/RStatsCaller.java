package com.itdev.statistic;

import com.github.rcaller.rstuff.RCaller;
import com.github.rcaller.rstuff.RCode;

import java.util.List;

public class RStatsCaller {

    private static final String HEAD = "p <- stats::%s(test_stat";
    private static final String TAIL = ", lower.tail = FALSE)";
    private static final String ONE_DF = ", df";
    private static final String TWO_DF = ", df1, df2";

    private double scriptBuild(String func, double testVal, List<Integer> dfs) {
        RCode rCode = RCode.create();

        rCode.addDouble("test_stat", testVal);
        String code = String.format(HEAD, func);
        int dfQuantity = dfs.size();
        boolean isPt = func.equals("pt");
        switch (dfQuantity) {
            case 0 -> {}
            case 1 -> {
                rCode.addInt("df", dfs.get(0));
                code += ONE_DF;
            }
            case 2 -> {
                rCode.addInt("df1", dfs.get(0));
                rCode.addInt("df2", dfs.get(1));
                code += TWO_DF;
            }
            default -> throw new IllegalArgumentException("There cannot be more than two degrees of freedom");
        }
        code += isPt ? ")" : TAIL;
        rCode.addRCode(code);
        return callR(rCode);
    }

    public double callRPNorm(double testVal) {
        return scriptBuild("pnorm", testVal, List.of());
    }

    public double callRPF(double testVal, int df1, int df2) {
        return scriptBuild("pf", testVal, List.of(df1, df2));
    }

    public double callRPT(double testVal, int df) {
        return scriptBuild("pt", testVal, List.of(df));
    }

    public double callRPChiSq(double testVal, int df) {
        return scriptBuild("pchisq", testVal, List.of(df));
    }

    private double callR(RCode code){
        RCaller caller = RCaller.create();

        caller.setRCode(code);
        caller.runAndReturnResult("p");

        return caller.getParser().getAsDoubleArray("p")[0];
    }
}
