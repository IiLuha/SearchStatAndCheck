package com.itdev.parser;

import com.github.rcaller.exception.ParseException;
import com.github.rcaller.rstuff.RCaller;
import com.itdev.exception.StatcheckException;
import com.itdev.statistics.StatcheckResultDO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class StatcheckResultParser {

    public List<StatcheckResultDO> parseResult(RCaller caller) throws StatcheckException {
        List<StatcheckResultDO> results = new ArrayList<>();

        // Получаем сырые данные из R
        String[] sources;
        try {
            sources = caller.getParser().getAsStringArray("source");
        } catch (ParseException e) {
//            e.printStackTrace();
            return results;
        }
        String[] testTypes = caller.getParser().getAsStringArray("test_type");
        String[] pComparisons = caller.getParser().getAsStringArray("p_comp");
        String[] df1sAsString = caller.getParser().getAsStringArray("df1");
        String[] df2sAsString = caller.getParser().getAsStringArray("df2");
        Integer[] df1s = getDfs(df1sAsString);
        Integer[] df2s = getDfs(df2sAsString);
        String[] testValuesAsString = caller.getParser().getAsStringArray("test_value");
        String[] reportedPsAsString = caller.getParser().getAsStringArray("reported_p");
        String[] computedPsAsString = caller.getParser().getAsStringArray("computed_p");
        Double[] testValues = getDouble(testValuesAsString);
        Double[] reportedPs = getDouble(reportedPsAsString);
        Double[] computedPs = getDouble(computedPsAsString);
        boolean[] errors = caller.getParser().getAsLogicalArray("error");
        boolean[] decisionErrors = caller.getParser().getAsLogicalArray("decision_error");
        boolean[] one_tailed = caller.getParser().getAsLogicalArray("one_tailed_in_txt");

        // Маппим в Java-объекты
        for (int i = 0; i < testTypes.length; i++) {
            StatcheckResultDO result = new StatcheckResultDO();
            result.setSource(sources[i]);
            result.setType(testTypes[i]);
            result.setDf1(df1s[i]);
            result.setDf2(df2s[i]);
            result.setTestValue(new BigDecimal(String.valueOf(testValues[i])).setScale(3, RoundingMode.HALF_UP));
            result.setPComparison(pComparisons[i]);
            result.setReportedP(new BigDecimal(String.valueOf(reportedPs[i])).setScale(3, RoundingMode.HALF_UP));
            result.setComputedP(new BigDecimal(String.valueOf(computedPs[i])).setScale(3, RoundingMode.HALF_UP));
            result.setError(errors[i]);
            result.setDecision_error(decisionErrors[i]);
            result.setOneTailed(one_tailed[i]);

            results.add(result);
        }
        return results;
    }

    private Double[] getDouble(String[] testValuesAsString) throws StatcheckException {
        Double[] dfs = new Double[testValuesAsString.length];
        for (int i = 0; i < testValuesAsString.length; i++) {
            String dfAsString = testValuesAsString[i];
            try {
                dfs[i] = Double.parseDouble(dfAsString);
            } catch (NumberFormatException e) {
                throw new StatcheckException(e.getMessage());
            }
        }
        return dfs;
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
