package com.itdev.parser;

import com.github.rcaller.exception.ParseException;
import com.github.rcaller.rstuff.RCaller;
import com.itdev.statistic.StatcheckResult;

import java.util.ArrayList;
import java.util.List;

public class StatcheckResultParser {

    public List<StatcheckResult> parseResult(RCaller caller) {
        List<StatcheckResult> results = new ArrayList<>();

        // Получаем сырые данные из R
        String[] sources;
        try {
            sources = caller.getParser().getAsStringArray("source");
        } catch (ParseException e) {
            e.printStackTrace();
            return results;
        }
        String[] testTypes = caller.getParser().getAsStringArray("test_type");
        String[] df1sAsString = caller.getParser().getAsStringArray("df1");
        String[] df2sAsString = caller.getParser().getAsStringArray("df2");
        Integer[] df1s = getDfs(df1sAsString);
        Integer[] df2s = getDfs(df2sAsString);
        double[] testValues = caller.getParser().getAsDoubleArray("test_value");
        String[] pComparisons = caller.getParser().getAsStringArray("p_comp");
        double[] reportedPs = caller.getParser().getAsDoubleArray("reported_p");
        double[] computedPs = caller.getParser().getAsDoubleArray("computed_p");
        boolean[] errors = caller.getParser().getAsLogicalArray("error");
        boolean[] decisionErrors = caller.getParser().getAsLogicalArray("decision_error");
        boolean[] one_tailed = caller.getParser().getAsLogicalArray("one_tailed_in_txt");

        // Маппим в Java-объекты
        for (int i = 0; i < testTypes.length; i++) {
            StatcheckResult result = new StatcheckResult();
            result.setSource(sources[i]);
            result.setType(testTypes[i]);
            result.setDf1(df1s[i]);
            result.setDf2(df2s[i]);
            result.setTestValue(testValues[i]);
            result.setPComparison(pComparisons[i]);
            result.setReportedP(reportedPs[i]);
            result.setComputedP(computedPs[i]);
            result.setError(errors[i]);
            result.setDecision_error(decisionErrors[i]);
            result.setOne_tailed(one_tailed[i]);

            results.add(result);
        }
        return results;
    }

    private Integer[] getDfs(String[] dfsAsString) {
        Integer[] dfs = new Integer[dfsAsString.length];
        for (int i = 0; i < dfsAsString.length; i++) {
            String dfAsString = dfsAsString[i];
            try {
                dfs[i] = Integer.parseInt(dfAsString);
            } catch (NumberFormatException ignored) {}
        }
        return dfs;
    }
}
