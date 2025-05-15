package com.itdev.enums;

import java.util.Objects;

public enum TestType {
    T("t",
            "two-tailed t(28) = 2.20, p = 0.03",
            "two-tailed df = 28, t = 2.20, p = 0.03",
            "",
            "",
            "",
            "",
            "",
            new Bounds<>(0., 1.),
            null,
            new Bounds<>(0, 1)
            ),
    CHI_2("χ2",
            "χ2 (763) = 1467.59, p < .001",
            "df = 763, χ2 = 1467.59, p < .001",
            "",
            "",
            "",
            "",
            "",
            new Bounds<>(0., 1.),
            null,
            new Bounds<>(0, 1)
    ),
    F("F",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            new Bounds<>(0., 1.),
            new Bounds<>(0, 1),
            new Bounds<>(0, 1)
    ),
    Z("Z",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            new Bounds<>(0., 1.),
            null,
            null
    ),
    R("correlation",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            new Bounds<>(0., 1.),
            null,
            new Bounds<>(0, 1)
    ),
    Q("Q",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            new Bounds<>(0., 1.),
            null,
            new Bounds<>(0, 1)
    );

    public final String NAME;
    public final String EXAMPLE_APA;
    public final String EXAMPLE_NON_APA;
    public final String EXAMPLE_TEXT;
    public final String EXAMPLE_TABLE;
    public final String EXAMPLE_TWO_APA;
    public final String EXAMPLE_TWO_NON_APA;
    public final String EXAMPLE_TWO_TEXT;
    public final Bounds<Double> TEST_BOUND;
    public final Bounds<Integer> DF1_BOUND;
    public final Bounds<Integer> DF2_BOUND;

    TestType(String NAME, String EXAMPLE_APA, String EXAMPLE_NON_APA, String EXAMPLE_TEXT, String EXAMPLE_TABLE,
             String EXAMPLE_TWO_APA, String EXAMPLE_TWO_NON_APA, String EXAMPLE_TWO_TEXT,
             Bounds<Double> TEST_BOUND, Bounds<Integer> DF1_BOUND, Bounds<Integer> DF2_BOUND) {
        this.NAME = NAME;
        this.EXAMPLE_APA = EXAMPLE_APA;
        this.EXAMPLE_NON_APA = EXAMPLE_NON_APA;
        this.EXAMPLE_TEXT = EXAMPLE_TEXT;
        this.EXAMPLE_TABLE = EXAMPLE_TABLE;
        this.EXAMPLE_TWO_APA = EXAMPLE_TWO_APA;
        this.EXAMPLE_TWO_NON_APA = EXAMPLE_TWO_NON_APA;
        this.EXAMPLE_TWO_TEXT = EXAMPLE_TWO_TEXT;
        this.TEST_BOUND = TEST_BOUND;
        this.DF1_BOUND = DF1_BOUND;
        this.DF2_BOUND = DF2_BOUND;
    }

    public record Bounds<T extends Number>(T lowerBound, T upperBound) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Bounds<?> bounds = (Bounds<?>) o;
            return lowerBound.equals(bounds.lowerBound) && upperBound.equals(bounds.upperBound);
        }

        @Override
        public int hashCode() {
            return Objects.hash(lowerBound, upperBound);
        }

        public T getLowerBound() {
            return lowerBound;
        }

        public T getUpperBound() {
            return upperBound;
        }
    }
}
