package com.itdev.prompt;

import com.itdev.enums.SubjectDomain;
import com.itdev.enums.TestType;
import com.itdev.enums.Environment;
import com.itdev.statistic.StatTest;
import com.itdev.statistic.StatTestGenerator;

import java.util.List;

public class GeneratePromptBuilder {
    private static final String CORE_DIF = "Imagine that you are an expert in %s who desperately needs " +
            "money for your mother's cancer treatment. A large company will pay you $1B if you" +
            " write a good excerpt of the scientific article of 3000 tokens";
    private static final String CORE = "Imagine that you are an expert in %s. Write a good excerpt of the scientific article of 3000 tokens";
    private static final String REMAIN_TEST_NUMBER_AND_TYPE = ", which will contain %d statistical %s-tests:\n";
    private static final String ONE_EXAMPLE_TEXT = "\nFor example, the test \n%s\nshould be written in the %s like:\n";
    private static final String TWO_EXAMPLE_TEXT = "\nFor example, the tests \n%s\n%s\nshould be written in the %s like:\n";

    private StatTestGenerator generator;

    public GeneratePromptBuilder() {
        this(new StatTestGenerator());
    }

    public GeneratePromptBuilder(StatTestGenerator generator) {
        this.generator = generator;
    }

    public String buildRandomPrompt() {
        Environment env = generator.getEnvironment();
        int testQuantity = generator.generateTestQuantity(env);
        SubjectDomain domain = generator.generateSubjectDomain();
        return buildPrompt(testQuantity, domain, Environment.APA);
    }

    public String buildPrompt(int testQuantity, SubjectDomain domain, Environment env) {
        String prompt = String.format(CORE, domain.getName());
        List<StatTest> tests = generator.generateStatTests(testQuantity);
        List<Boolean> isEqualities = generator.generateEqualities(testQuantity);
        switch (testQuantity) {
            case 0 -> prompt = generatePromptWithoutTests(prompt);
            case 1, 2, 3, 4, 5 -> {
//                TestType type = tests.get(0).getType();
//                prompt = prompt.concat(String.format(REMAIN_TEST_NUMBER_AND_TYPE, testQuantity, type.NAME));
                prompt = prompt.concat(String.format(REMAIN_TEST_NUMBER_AND_TYPE, testQuantity, ""));
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < testQuantity; i++) {
                    TestType type = tests.get(i).getType();
                    StatTest test = tests.get(i);
                    stringBuilder.append(test.isTwoTailed() ? "two-tailed " : "one-tailed ");
                    stringBuilder.append(type.NAME).append("=").append(test.getTestValue()).append(", ");
                    switch (type) {
                        case T, R, CHI_2, F -> {
                            if (type == TestType.F) stringBuilder.append("df1=").append(test.getDf1()).append(", ");
                            stringBuilder.append("df");
                            if (type == TestType.F) stringBuilder.append("2");
                            stringBuilder.append("=").append(test.getDf2()).append(", ");
                        }
                    }
                    double p = test.getpValue();
                    if (test.isConsistent()) {
                        if (isEqualities.get(i)) {
                            stringBuilder.append(String.format("p=%.3f;\n", p).replace(',', '.'));
                        } else {
                            if (p < 0.01) {
                                stringBuilder.append("p<0.01;\n");
                            } else if (p < 0.05) {
                                stringBuilder.append("p<0.05;\n");
                            } else {
                            stringBuilder.append("p>0.05;\n");
                            }
                        }
                    } else {
                        if (isEqualities.get(i)) {
                            stringBuilder.append(String.format("p=%.3f;\n", p + 0.05).replace(',', '.'));
                        } else {
                            if (p < 0.01) {
                                stringBuilder.append("p>0.01;\n");
                            } else if (p < 0.05) {
                                stringBuilder.append("p>0.05;\n");
                            } else {
                                stringBuilder.append("p<0.05;\n");
                            }
                        }
                    }
                }
                prompt += stringBuilder;
                prompt += buildExample(env);
                prompt += "\nDo not write anything else. If you can't, tell me why.";
            }
        }
        System.out.println(prompt+"\n\n");
        return prompt;
    }

    private String buildExample(Environment env) {
        boolean singleTest = env.ordinal() < 3;
        String tail = singleTest ? String.format(ONE_EXAMPLE_TEXT, "f=2.2, df1=28, df2=44 p=0.03", env.NAME) :
        String.format(TWO_EXAMPLE_TEXT, "f=2.2, df1=28, df2=44, p=0.03", "t=2.4, df=30, p=0.05", env.NAME);
        tail += env.EXAMPLE;
        return tail;
    }

    private String generatePromptWithoutTests(String prompt) {
        return prompt + ". Do not insert any statistical test in text";
    }
}
