package com.itdev.statistics;

import com.itdev.enums.TestType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Objects;

@Data
public class StatTest {
    private TestType type;
    private boolean oneTailed;
    private BigDecimal testValue;
    private Integer df1;
    private Integer df2;
    private BigDecimal pValue;
    private boolean consistent;
    private boolean isEquality;

    public StatTest(TestType type, boolean oneTailed, BigDecimal testValue, BigDecimal pValue, boolean consistent, boolean isEquality) {
        this(type, oneTailed, testValue, null, pValue, consistent, isEquality);
    }

    public StatTest(TestType type, BigDecimal testValue, Integer df2, BigDecimal pValue, boolean consistent, boolean isEquality) {
        this(type, false, testValue, df2, pValue, consistent, isEquality);
    }

    public StatTest(TestType type, boolean oneTailed, BigDecimal testValue, Integer df2, BigDecimal pValue, boolean consistent, boolean isEquality) {
        this(type, oneTailed, testValue, null, df2, pValue, consistent, isEquality);
    }

    public StatTest(TestType type, BigDecimal testValue, Integer df1, Integer df2, BigDecimal pValue, boolean consistent, boolean isEquality) {
        this(type, false, testValue, df1, df2, pValue, consistent, isEquality);
    }

    public StatTest(TestType type, boolean oneTailed, BigDecimal testValue, Integer df1, Integer df2, BigDecimal pValue, boolean consistent, boolean isEquality) {
        this.type = type;
        this.oneTailed = oneTailed;
        this.testValue = testValue;
        this.df1 = df1;
        this.df2 = df2;
        this.pValue = pValue;
        this.consistent = consistent;
        this.isEquality = isEquality;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatTest statTest = (StatTest) o;
        return oneTailed == statTest.oneTailed && type == statTest.type && Objects.equals(testValue, statTest.testValue) && Objects.equals(df1, statTest.df1) && Objects.equals(df2, statTest.df2) && Objects.equals(pValue, statTest.pValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, oneTailed, testValue, df1, df2, pValue);
    }

    @Override
    public String toString() {
        return "StatTest{" +
                "type=" + type +
                ", oneTailed=" + oneTailed +
                ", testValue=" + testValue +
                ", df1=" + df1 +
                ", df2=" + df2 +
                ", pValue=" + pValue +
                ", consistent=" + consistent +
                ", isEqualities=" + isEquality +
                '}';
    }
}
