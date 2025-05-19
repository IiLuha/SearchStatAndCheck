package com.itdev.prompt;

import com.itdev.enums.Environment;
import com.itdev.enums.SubjectDomain;
import com.itdev.enums.TestType;
import com.itdev.statistic.StatTest;
import com.itdev.statistic.StatTestGenerator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class GeneratePromptBuilderTest {

    private GeneratePromptBuilder builder;
    private StatTestGenerator generator;

    @BeforeEach
    void prepare() {
        generator = mock(StatTestGenerator.class);
        builder = new GeneratePromptBuilder(generator);
    }

    @Test
    void createGeneratePromptWithoutTests() {
        //given
        Environment env = Environment.TEXT;
        int testQuantity = 0;
        SubjectDomain domain = SubjectDomain.values()[2];
        String expected = "Imagine that you are an expert in economics and finance. Write a good excerpt of " +
                "the scientific article of 3000 tokens. Do not insert any statistical test in text";

        //when
        String actual = builder.buildPrompt(testQuantity, domain, env);

        //then
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void createGeneratePromptWithOneTestInNonAPAAndIsEquality() {
        //given
        Environment env = Environment.NON_APA;
        int testQuantity = 1;
        List<Boolean> isEqualities = List.of(true);
        SubjectDomain domain = SubjectDomain.values()[1];
        String expected = """
                Imagine that you are an expert in psychology and social sciences. Write a good excerpt of the scientific article of 3000 tokens, which will contain 1 statistical t-tests:
                one-tailed t=2.2, df=28, p=0.030;
                                
                For example, the test\s
                f=2.2, df1=28, df2=44 p=0.03
                should be written in the not APA-style like:
                two-tailed df1 = 28, df2 = 44, f = 2.20, p = 0.03
                Do not write anything else. If you can't, tell me why.""";
        List<StatTest> tests = new ArrayList<>();
        tests.add(new StatTest(
                TestType.T, false, 2.2, 28, 0.030, true));
        doReturn(tests).when(generator).generateStatTests(testQuantity);
        doReturn(isEqualities).when(generator).generateEqualities(testQuantity);

        //when
        String actual = builder.buildPrompt(testQuantity, domain, env);

        //then
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void createGeneratePromptWithOneTestInTextAndIsEquality() {
        //given
        Environment env = Environment.TEXT;
        int testQuantity = 1;
        List<Boolean> isEqualities = List.of(true);
        SubjectDomain domain = SubjectDomain.values()[1];
        String expected = """
                Imagine that you are an expert in psychology and social sciences. Write a good excerpt of the scientific article of 3000 tokens, which will contain 1 statistical t-tests:
                one-tailed t=2.2, df=28, p=0.030;
                                
                For example, the test\s
                f=2.2, df1=28, df2=44 p=0.03
                should be written in the text like:
                A two-tailed f-test showed a statistically significant result with a f value of 2.20 at 28, 44 degrees of freedom. The p-value was less than 0.05.
                Do not write anything else. If you can't, tell me why.""";
        List<StatTest> tests = new ArrayList<>();
        tests.add(new StatTest(
                TestType.T, false, 2.2, 28, 0.030, true));
        doReturn(tests).when(generator).generateStatTests(testQuantity);
        doReturn(isEqualities).when(generator).generateEqualities(testQuantity);

        //when
        String actual = builder.buildPrompt(testQuantity, domain, env);

        //then
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void createGeneratePromptWithOneTestInAPAAndIsNotEquality() {
        //given
        Environment env = Environment.APA;
        int testQuantity = 1;
        List<Boolean> isEqualities = List.of(false);
        SubjectDomain domain = SubjectDomain.values()[1];
        String expected = """
                Imagine that you are an expert in psychology and social sciences. Write a good excerpt of the scientific article of 3000 tokens, which will contain 1 statistical t-tests:
                one-tailed t=2.2, df=28, p<0.05;
                                
                For example, the test\s
                f=2.2, df1=28, df2=44 p=0.03
                should be written in the APA-style like:
                two-tailed f(28, 44) = 2.20, p = 0.03
                Do not write anything else. If you can't, tell me why.""";
        List<StatTest> tests = new ArrayList<>();
        tests.add(new StatTest(
                TestType.T, false, 2.2, 28, 0.030, true));
        doReturn(tests).when(generator).generateStatTests(testQuantity);
        doReturn(isEqualities).when(generator).generateEqualities(testQuantity);

        //when
        String actual = builder.buildPrompt(testQuantity, domain, env);

        //then
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void createGeneratePromptWithFiveTestsInTable() {
        //given
        Environment env = Environment.TABLE;
        int testQuantity = 5;
        List<Boolean> isEqualities = List.of(false, false, false, false, false);
        SubjectDomain domain = SubjectDomain.values()[0];
        String expected = """
            Imagine that you are an expert in medicine and biology. Write a good excerpt of the scientific article of 3000 tokens, which will contain 5 statistical t-tests:
            two-tailed t=1.2, df=27, p>0.05;
            two-tailed t=2.2, df=28, p<0.05;
            one-tailed t=3.2, df=29, p<0.01;
            two-tailed t=4.2, df=30, p<0.05;
            one-tailed t=5.2, df=31, p>0.05;
            
            For example, the tests\s
            f=2.2, df1=28, df2=44, p=0.03
            t=2.4, df=30, p=0.05
            should be written in the table like:
            someValue test df1 df2 p
            144 2.2 28 44 0.03
            255 2.4 30 - 0.05
            
            Do not write anything else. If you can't, tell me why.""";
        List<StatTest> tests = new ArrayList<>();
        tests.add(new StatTest(TestType.T, true, 1.2, 27, 0.02, false));
        tests.add(new StatTest(TestType.T, true, 2.2, 28, 0.03, true));
        tests.add(new StatTest(TestType.T, false, 3.2, 29, 0.005, true));
        tests.add(new StatTest(TestType.T, true, 4.2, 30, 0.07, false));
        tests.add(new StatTest(TestType.T, false, 5.2, 31, 0.06, true));
        doReturn(tests).when(generator).generateStatTests(testQuantity);
        doReturn(isEqualities).when(generator).generateEqualities(testQuantity);

        //when
        String actual = builder.buildPrompt(testQuantity, domain, env);

        //then
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void createGeneratePromptWithTwoTestsInTwoText() {
        //given
        Environment env = Environment.TWO_TEXT;
        int testQuantity = 2;
        List<Boolean> isEqualities = List.of(false, false);
        SubjectDomain domain = SubjectDomain.values()[0];
        String expected = """
                Imagine that you are an expert in medicine and biology. Write a good excerpt of the scientific article of 3000 tokens, which will contain 2 statistical t-tests:
                two-tailed t=1.2, df=27, p>0.05;
                two-tailed t=2.2, df=28, p<0.05;
                                
                For example, the tests\s
                f=2.2, df1=28, df2=44, p=0.03
                t=2.4, df=30, p=0.05
                should be written in the text like:
                The two two-tailed f-tests showed a statistically significant result with a f value of 2.20 and 2.4 at 28, 44 and 30, 46 degrees of freedom. The p-value was less than 0.05 for them both.
                Do not write anything else. If you can't, tell me why.""";
        List<StatTest> tests = new ArrayList<>();
        tests.add(new StatTest(TestType.T, true, 1.2, 27, 0.02, false));
        tests.add(new StatTest(TestType.T, true, 2.2, 28, 0.03, true));
        doReturn(tests).when(generator).generateStatTests(testQuantity);
        doReturn(isEqualities).when(generator).generateEqualities(testQuantity);

        //when
        String actual = builder.buildPrompt(testQuantity, domain, env);

        //then
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void createGeneratePromptWithTwoTestsInTwoAPA() {
        //given
        Environment env = Environment.TWO_APA;
        int testQuantity = 2;
        List<Boolean> isEqualities = List.of(false, false);
        SubjectDomain domain = SubjectDomain.values()[0];
        String expected = """
                Imagine that you are an expert in medicine and biology. Write a good excerpt of the scientific article of 3000 tokens, which will contain 2 statistical t-tests:
                two-tailed t=4.2, df=30, p<0.05;
                one-tailed t=5.2, df=31, p>0.05;
                                
                For example, the tests\s
                f=2.2, df1=28, df2=44, p=0.03
                t=2.4, df=30, p=0.05
                should be written in the APA-style like:
                two-tailed f(28, 44) = 2.20 and t(30) = 2.4, p = 0.03 and p = 0.05
                Do not write anything else. If you can't, tell me why.""";
        List<StatTest> tests = new ArrayList<>();
        tests.add(new StatTest(TestType.T, true, 4.2, 30, 0.07, false));
        tests.add(new StatTest(TestType.T, false, 5.2, 31, 0.06, true));
        doReturn(tests).when(generator).generateStatTests(testQuantity);
        doReturn(isEqualities).when(generator).generateEqualities(testQuantity);

        //when
        String actual = builder.buildPrompt(testQuantity, domain, env);

        //then
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void createGeneratePromptWithTwoTestsInTwoNonAPA() {
        //given
        Environment env = Environment.TWO_NON_APA;
        int testQuantity = 2;
        List<Boolean> isEqualities = List.of(false, false);
        SubjectDomain domain = SubjectDomain.values()[0];
        String expected = """
                Imagine that you are an expert in medicine and biology. Write a good excerpt of the scientific article of 3000 tokens, which will contain 2 statistical t-tests:
                two-tailed t=1.2, df=27, p>0.05;
                one-tailed t=5.2, df=31, p>0.05;
                                
                For example, the tests\s
                f=2.2, df1=28, df2=44, p=0.03
                t=2.4, df=30, p=0.05
                should be written in the not APA-style like:
                two-tailed df1 = 28 and 30, df2 = 44, f = 2.20 and t = 2.4, p = 0.03 and 0.05
                Do not write anything else. If you can't, tell me why.""";
        List<StatTest> tests = new ArrayList<>();
        tests.add(new StatTest(TestType.T, true, 1.2, 27, 0.02, false));
        tests.add(new StatTest(TestType.T, false, 5.2, 31, 0.06, true));
        doReturn(tests).when(generator).generateStatTests(testQuantity);
        doReturn(isEqualities).when(generator).generateEqualities(testQuantity);

        //when
        String actual = builder.buildPrompt(testQuantity, domain, env);

        //then
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void createGeneratePromptWithFiveSingleTestsInAPA() {
        //given
        Environment env = Environment.APA;
        int testQuantity = 5;
        List<Boolean> isEqualities = List.of(false, false, true, false, false);
        SubjectDomain domain = SubjectDomain.values()[0];
        String expected = """
                Imagine that you are an expert in medicine and biology. Write a good excerpt of the scientific article of 3000 tokens, which will contain 5 statistical t-tests:
                two-tailed t=1.2, df=27, p>0.05;
                two-tailed t=2.2, df=28, p<0.05;
                one-tailed t=3.2, df=29, p=0.005;
                two-tailed t=4.2, df=30, p<0.05;
                one-tailed t=5.2, df=31, p>0.05;
                                
                For example, the test\s
                f=2.2, df1=28, df2=44 p=0.03
                should be written in the APA-style like:
                two-tailed f(28, 44) = 2.20, p = 0.03
                Do not write anything else. If you can't, tell me why.""";
        List<StatTest> tests = new ArrayList<>();
        tests.add(new StatTest(TestType.T, true, 1.2, 27, 0.02, false));
        tests.add(new StatTest(TestType.T, true, 2.2, 28, 0.03, true));
        tests.add(new StatTest(TestType.T, false, 3.2, 29, 0.005, true));
        tests.add(new StatTest(TestType.T, true, 4.2, 30, 0.07, false));
        tests.add(new StatTest(TestType.T, false, 5.2, 31, 0.06, true));
        doReturn(tests).when(generator).generateStatTests(testQuantity);
        doReturn(isEqualities).when(generator).generateEqualities(testQuantity);

        //when
        String actual = builder.buildPrompt(testQuantity, domain, env);

        //then
        Assertions.assertThat(actual).isEqualTo(expected);
    }
}
