package com.itdev.enums;

import java.util.Objects;

public enum TestType {
    T("t",
            null,
            new Bounds<>(10, 50)
            ),
    CHI_2("Chi2",

            new Bounds<>(30, 1500),
            null
    ),
    F("F",
            new Bounds<>(2, 15),
            new Bounds<>(10, 30)
    ),
    Z("Z",
            null,
            null
    ),
    R("correlation",
            null,
            new Bounds<>(10, 50)
    ),
    Q("Q",
            new Bounds<>(30, 1500),
            null
    );

    public final String NAME;
    public final Bounds<Integer> DF1_BOUND;
    public final Bounds<Integer> DF2_BOUND;

    TestType(String NAME, Bounds<Integer> DF1_BOUND, Bounds<Integer> DF2_BOUND) {
        this.NAME = NAME;
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
