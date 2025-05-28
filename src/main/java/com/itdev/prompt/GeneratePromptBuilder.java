package com.itdev.prompt;

import com.itdev.enums.SubjectDomain;
import com.itdev.enums.TestType;
import com.itdev.enums.Environment;
import com.itdev.statistic.StatTest;
import com.itdev.statistic.StatTestGenerator;

import java.math.BigDecimal;
import java.util.List;

public class GeneratePromptBuilder {
    private static final String CORE_DIF = "Imagine that you are an expert in %s who desperately needs " +
            "money for your mother's cancer treatment. A large company will pay you $1B if you" +
            " write a good excerpt of the scientific article of 3000 tokens";
    private static final String CORE = "Imagine that you are an expert in %s. Write a good excerpt of the scientific article of 3000 tokens";
    private static final String REMAIN_TEST_NUMBER_AND_TYPE = ", which will contain %d statistical tests:\n";
    private static final String FIRST_TEST_EXAMPLE = "f=2.2, df1=28, df2=44 p=0.03";
    private static final String ONE_EXAMPLE_TEXT = "\nFor example (do not insert next example in excerpt, use only the format for writing previous tests in the excerpt), the test \n%s\nshould be written in the %s like:\n";
    private static final String SECOND_TEST_EXAMPLE = "one-tailed t=2.4, df=30, p=0.04";
    private static final String TWO_EXAMPLE_TEXT = "\nFor example (do not insert next examples in the excerpt, use only the format for writing previous tests in the excerpt), the tests \n%s\n%s\nshould be written in the %s like:\n";
    private static final String NOT_DUPLICATE = "\nDo not write the same test two or more times (only one time)\n";
    private static final String NOT_USE_APA = "\nDo not use APA-style for writing tests.";
    private static final String TAIL = "\nDo not write anything else. If you can't, tell me why.";
    private static final BigDecimal ALPHA_0_01 = new BigDecimal("0.01");
    private static final BigDecimal ALPHA_0_05 = new BigDecimal("0.05");

    private StatTestGenerator generator;

    public GeneratePromptBuilder() {
        this(new StatTestGenerator());
    }

    public GeneratePromptBuilder(StatTestGenerator generator) {
        this.generator = generator;
    }

    public String buildRandomPrompt() {
        Environment env = generator.getEnvironment();
        return buildRandomPrompt(env);
    }

    public String buildRandomPrompt(int testQuantity) {
        Environment env = generator.getEnvironment();
        return buildRandomPrompt(env, testQuantity);
    }

    public String buildRandomPrompt(Environment env, int testQuantity) {
        List<StatTest> tests = generator.generateStatTests(testQuantity);
        return buildRandomPrompt(env, tests);
    }

    public String buildRandomPrompt(Environment env) {
        int testQuantity = generator.generateTestQuantity(env);
        List<StatTest> tests = generator.generateStatTests(testQuantity);
        return buildRandomPrompt(env, tests);
    }

    public String buildRandomPrompt(Environment env, List<StatTest> tests) {
        SubjectDomain domain = generator.generateSubjectDomain();
        return buildPrompt(tests, domain, env);
    }

    public String buildPrompt(List<StatTest> tests, SubjectDomain domain, Environment env) {
        int testQuantity = tests.size();
        String prompt = String.format(CORE, domain.getName());
        switch (testQuantity) {
            case 0 -> prompt = generatePromptWithoutTests(prompt);
            case 1, 2, 3, 4, 5 -> {
//                TestType type = tests.get(0).getType();
//                prompt = prompt.concat(String.format(REMAIN_TEST_NUMBER_AND_TYPE, testQuantity, type.NAME));
                prompt = prompt.concat(String.format(REMAIN_TEST_NUMBER_AND_TYPE, testQuantity));
                StringBuilder stringBuilder = new StringBuilder();
                for (StatTest statTest : tests) {
                    TestType type = statTest.getType();
                    if ((type == TestType.T || type == TestType.Z || type == TestType.R) && statTest.isOneTailed()) {
                        stringBuilder.append("one-tailed ");
                    }
                    stringBuilder.append(type.NAME).append("=");
                    stringBuilder.append(statTest.getTestValue().toPlainString().replace(',', '.'))
                            .append(", ");
                    switch (type) {
                        case T, R, CHI2, F, Q -> {
                            if (type == TestType.F) stringBuilder.append("df1=").append(statTest.getDf1()).append(", ");
                            stringBuilder.append("df");
                            if (type == TestType.F) stringBuilder.append("2");
                            stringBuilder.append("=").append(statTest.getDf2()).append(", ");
                        }
                    }
                    BigDecimal p = statTest.getPValue();
                    if (statTest.isConsistent()) {
                        if (statTest.isEquality()) {
                            stringBuilder.append("p=")
                                    .append(p.toPlainString().replace(',', '.'))
                                    .append("\n");
                        } else {
                            if (p.compareTo(ALPHA_0_01) < 0) {
                                stringBuilder.append("p<0.01;\n");
                            } else if (p.compareTo(ALPHA_0_05) < 0) {
                                stringBuilder.append("p<0.05;\n");
                            } else {
                                stringBuilder.append("p>0.05;\n");
                            }
                        }
                    } else {
                        if (statTest.isEquality()) {
                            stringBuilder.append("p=")
                                    .append(p.add(ALPHA_0_05).toPlainString().replace(',', '.'))
                                    .append("\n");
                        } else {
                            if (p.compareTo(ALPHA_0_01) < 0) {
                                stringBuilder.append("p>0.01;\n");
                            } else if (p.compareTo(ALPHA_0_05) < 0) {
                                stringBuilder.append("p>0.05;\n");
                            } else {
                                stringBuilder.append("p<0.05;\n");
                            }
                        }
                    }
                }
                stringBuilder.append(NOT_DUPLICATE);
                prompt += stringBuilder;
                prompt += buildExample(env);
                if (!(env.equals(Environment.APA) || env.equals(Environment.TWO_APA))) prompt += NOT_USE_APA;
                prompt += TAIL;
            }
        }
        System.out.println(prompt + "\n\n");
        return prompt;
    }

    private String buildExample(Environment env) {
        boolean singleTest = env.ordinal() < 3;
        String tail = singleTest ? String.format(ONE_EXAMPLE_TEXT, FIRST_TEST_EXAMPLE, env.NAME) :
                String.format(TWO_EXAMPLE_TEXT, FIRST_TEST_EXAMPLE, SECOND_TEST_EXAMPLE, env.NAME);
        tail += env.EXAMPLE;
        return tail;
    }

    private String generatePromptWithoutTests(String prompt) {
        return prompt + ". Do not insert any statistical test in text";
    }
}
