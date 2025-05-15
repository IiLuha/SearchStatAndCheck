package com.itdev.enums;

import java.util.Objects;

public enum TestType {
    T("two-tailed t(28) = 2.20, p = 0.03",
            "two-tailed df = 28, t = 2.20, p = 0.03",
            "",
            "",
            "",
            "",
            "",
            new Bounds<>(0., 1.),
            new Bounds<>(0, 1),
            new Bounds<>(0, 1)
            ),
    CHI("χ2 (763) = 1467.59, p < .001",
            "df = 763, χ2 = 1467.59, p < .001",
            "",
            "",
            "",
            "",
            "",
            new Bounds<>(0., 1.),
            new Bounds<>(0, 1),
            new Bounds<>(0, 1)
    ),
    F("",
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
    Z("",
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
    R("",
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
    Q("",
            "",
            "",
            "",
            "",
            "",
            "",
            new Bounds<>(0., 1.),
            new Bounds<>(0, 1),
            new Bounds<>(0, 1)
    );

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

    TestType(String EXAMPLE_APA, String EXAMPLE_NON_APA, String EXAMPLE_TEXT, String EXAMPLE_TABLE,
             String EXAMPLE_TWO_APA, String EXAMPLE_TWO_NON_APA, String EXAMPLE_TWO_TEXT,
             Bounds<Double> TEST_BOUND, Bounds<Integer> DF1_BOUND, Bounds<Integer> DF2_BOUND) {
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

    public static class Bounds<T extends Number> {
        private T lowerBound;
        private T upperBound;

        public Bounds(T lowerBound, T upperBound) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

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

        public void setLowerBound(T lowerBound) {
            this.lowerBound = lowerBound;
        }

        public T getUpperBound() {
            return upperBound;
        }

        public void setUpperBound(T upperBound) {
            this.upperBound = upperBound;
        }
    }
}
