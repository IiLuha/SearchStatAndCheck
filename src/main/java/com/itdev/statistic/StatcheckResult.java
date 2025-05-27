package com.itdev.statistic;

import com.itdev.enums.TestType;

/**
 * Класс для хранения результатов statcheck (NHST-тесты).
 * Пример строки из statcheck: "t(24) = 2.34, p = .03"
 */
public class StatcheckResult {
    private String source;       // Номер теста в статье
    private String type;    // Тип теста
    private Integer df1;  // Первая степень свободы (если есть)
    private Integer df2;  // Вторая степень свободы (для F-теста, может быть null)
    private double testValue;    // Значение статистики (2.34)
    private String pComparison;  // Оператор сравнения p (=, <, >)
    private double reportedP;    // Заявленное p-значение (0.03)
    private double computedP;    // Заявленное p-значение (0.03)
    private boolean error;       //
    private boolean decision_error;       //
    private boolean one_tailed;       //
    private int apaFactor;

    public StatcheckResult() {
    }

    @Override
    public String toString() {
        return "StatcheckResult{" +
                "source='" + source + '\'' +
                ", type='" + type + '\'' +
                ", df1=" + df1 +
                ", df2=" + df2 +
                ", testValue=" + testValue +
                ", pComparison='" + pComparison + '\'' +
                ", reportedP=" + reportedP +
                ", computedP=" + computedP +
                ", error=" + error +
                ", decision_error=" + decision_error +
                ", one_tailed=" + one_tailed +
                ", apaFactor=" + apaFactor +
                '}';
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getDf1() {
        return df1;
    }

    public void setDf1(Integer df1) {
        this.df1 = df1;
    }

    public Integer getDf2() {
        return df2;
    }

    public void setDf2(Integer df2) {
        this.df2 = df2;
    }

    public double getTestValue() {
        return testValue;
    }

    public void setTestValue(double testValue) {
        this.testValue = testValue;
    }

    public String getPComparison() {
        return pComparison;
    }

    public void setPComparison(String pComparison) {
        this.pComparison = pComparison;
    }

    public double getReportedP() {
        return reportedP;
    }

    public void setReportedP(double reportedP) {
        this.reportedP = reportedP;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public boolean isDecision_error() {
        return decision_error;
    }

    public void setDecision_error(boolean decision_error) {
        this.decision_error = decision_error;
    }

    public boolean isOneTailed() {
        return one_tailed;
    }

    public void setOne_tailed(boolean one_tailed) {
        this.one_tailed = one_tailed;
    }

    public int getApaFactor() {
        return apaFactor;
    }

    public void setApaFactor(int apaFactor) {
        this.apaFactor = apaFactor;
    }

    public double getComputedP() {
        return computedP;
    }

    public void setComputedP(double computedP) {
        this.computedP = computedP;
    }
}
