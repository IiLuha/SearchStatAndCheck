package com.itdev.prompt;

import com.itdev.enums.SubjectDomain;
import com.itdev.enums.TestType;
import com.itdev.statiatic.StatTest;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.random;

public class GeneratePromptBuilder {
    private static final String CORE = "1.\tYou are an expert in %s who desperately needs " +
            "money for your mother's cancer treatment. A large company will pay you $1B if you" +
            " write an excerpt of the article of 3000 tokens";
    private static final String REMAIN_TEST_NUMBER_AND_VALUES = ", which will contain %d statistical %s-tests:\n";
    private static final String REMAIN_DEGREES_OF_FREEDOM = "; degrees of freedom: ";
    private static final String REMAIN_P_VALUE = " and p-value written in the form";

    public String buildGeneratePrompt() {
        int numberOfTests = (int) (random() * 6);
        int numberOfDomain = (int) (random() * 3);
        SubjectDomain domain = SubjectDomain.values()[numberOfDomain];
        String prompt = String.format(CORE, domain.name());
        List<StatTest> tests = generateStatTests(numberOfTests);
        switch (numberOfTests) {
            case 0 -> prompt = generatePromptWithoutTests(prompt);
            case 1, 2, 3, 4, 5 -> {
                TestType type = tests.get(0).getType();
                prompt = prompt.concat(String.format(REMAIN_TEST_NUMBER_AND_VALUES, numberOfTests, type.name()));
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < numberOfTests; i++) {
                    StatTest test = tests.get(i);
                    stringBuilder.append(type.name()).append("=").append(test.getTestValue()).append(", ");
                    switch (type) {
                        case T, R, CHI, F -> {
                            if (type == TestType.F) stringBuilder.append("df1=").append(test.getDf1()).append(", ");
                            stringBuilder.append("df");
                            if (type == TestType.F) stringBuilder.append("2");
                            stringBuilder.append("=").append(test.getDf2()).append(", ");
                        }
                    }
                    stringBuilder.append("p=").append(test.getpValue()).append(";\n");
                }
                prompt += stringBuilder;
            }
            default -> throw new IllegalArgumentException("numberOfTests must be from 0 to 5, but is "+numberOfTests);
        }
        return prompt;
    }

    private List<StatTest> generateStatTests(int numberOfTests) {
        ArrayList<StatTest> tests = new ArrayList<>(numberOfTests);
        for (int i = 0; i < numberOfTests; i++) {
            TestType type = TestType.values()[(int) (random() * TestType.values().length)];
            double testVal = random() * (type.TEST_BOUND.getUpperBound() - type.TEST_BOUND.getLowerBound()) + type.TEST_BOUND.getLowerBound();
            double p = random();
            boolean twoTailed = (int) (random() * 2) == 1;
            switch (type) {
                case Z, Q -> tests.add(new StatTest(type, twoTailed, testVal, p));
                case T, R, CHI -> {
                    int df2 = (int) (random() * (type.DF2_BOUND.getUpperBound() - type.DF2_BOUND.getLowerBound())) + type.DF2_BOUND.getLowerBound();
                    tests.add(new StatTest(type, twoTailed, testVal, df2, p));
                }
                case F -> {
                    int df1 = (int) (random() * (type.DF1_BOUND.getUpperBound() - type.DF1_BOUND.getLowerBound())) + type.DF1_BOUND.getLowerBound();
                    int df2 = (int) (random() * (type.DF2_BOUND.getUpperBound() - type.DF2_BOUND.getLowerBound())) + type.DF2_BOUND.getLowerBound();
                    tests.add(new StatTest(type, twoTailed, testVal, df1, df2, p));
                }
            }
        }
        return tests;
    }

    private String generatePromptWithoutTests(String prompt) {
        return prompt;
    }
}
