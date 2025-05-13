package com.itdev.statiatic;

import com.itdev.enums.TestType;

import java.util.Objects;

public class StatTest {
    private TestType type;
    private boolean isTwoTailed;
    private Double testValue;
    private Integer df1;
    private Integer df2;
    private Double pValue;

    public StatTest(TestType type, boolean isTwoTailed, Double testValue, Double pValue) {
        this(type, isTwoTailed, testValue, null, pValue);
    }

    public StatTest(TestType type, boolean isTwoTailed, Double testValue, Integer df2, Double pValue) {
        this(type, isTwoTailed, testValue, null, df2, pValue);
    }

    public StatTest(TestType type, boolean isTwoTailed, Double testValue, Integer df1, Integer df2, Double pValue) {
        this.type = type;
        this.isTwoTailed = isTwoTailed;
        this.testValue = testValue;
        this.df1 = df1;
        this.df2 = df2;
        this.pValue = pValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatTest statTest = (StatTest) o;
        return isTwoTailed == statTest.isTwoTailed && type == statTest.type && Objects.equals(testValue, statTest.testValue) && Objects.equals(df1, statTest.df1) && Objects.equals(df2, statTest.df2) && Objects.equals(pValue, statTest.pValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, isTwoTailed, testValue, df1, df2, pValue);
    }

    public TestType getType() {
        return type;
    }

    public void setType(TestType type) {
        this.type = type;
    }

    public boolean isTwoTailed() {
        return isTwoTailed;
    }

    public void setTwoTailed(boolean twoTailed) {
        isTwoTailed = twoTailed;
    }

    public Double getTestValue() {
        return testValue;
    }

    public void setTestValue(Double testValue) {
        this.testValue = testValue;
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

    public Double getpValue() {
        return pValue;
    }

    public void setpValue(Double pValue) {
        this.pValue = pValue;
    }

    @Override
    public String toString() {
        return "StatTest{" +
                "testType=" + type +
                ", " + (isTwoTailed ? "twoTailed" : "oneTailed") +
                ", testValue=" + testValue +
                ", df1=" + df1 +
                ", df2=" + df2 +
                ", p-value=" + pValue +
                '}';
    }
}
