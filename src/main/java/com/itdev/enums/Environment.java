package com.itdev.enums;

public enum Environment {
    APA("APA-style",
            "f(28, 44) = 2.20, p = 0.03"),
    NON_APA("not APA-style",
            "df1 = 28, df2 = 44, f = 2.20, p = 0.03"),
    TEXT("text",
            "A f-test showed a statistically significant " +
            "result with a f value of 2.20 at 28, 44 degrees of freedom. The p-value was less than 0.05."),
    TABLE("table",
            """
                    someValue one-tailed test df1 df2 p
                    144 - 2.2 28 44 0.03
                    255 yes 2.4 30 - 0.05
                    """),
    TWO_APA("APA-style",
            "f(28, 44) = 2.20 and one-tailed t(30) = 2.4, " +
            "p = 0.03 and p = 0.05"),
    TWO_NON_APA("not APA-style",
            " df1 = 28 and 30, df2 = 44, " +
            "f = 2.20 and one-tailed t = 2.4, p = 0.03 and 0.05"),
    TWO_TEXT("text",
            "The f-test and one-tailed t-test showed a statistically " +
            "significant result with a f and p value of 2.20 and 2.4 at 28, 44 and 30 degrees of freedom. The " +
            "p-value was less than 0.05 for them both.");

    public final String NAME;
    public final String EXAMPLE;

    Environment(String NAME, String EXAMPLE) {
        this.NAME = NAME;
        this.EXAMPLE = EXAMPLE;
    }
}
