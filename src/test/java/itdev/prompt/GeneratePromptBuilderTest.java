package itdev.prompt;

import com.itdev.enums.SubjectDomain;
import com.itdev.enums.TestType;
import com.itdev.prompt.GeneratePromptBuilder;
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
        int testQuantity = 0;
        SubjectDomain domain = SubjectDomain.values()[2];
        String expected = "Imagine that you are an expert in economics and finance. Write a good excerpt of " +
                "the scientific article of 3000 tokens. Do not insert any statistical test in text";

        //when
        String actual = builder.buildPrompt(testQuantity, domain);

        //then
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void createGeneratePromptWithOneTestAndIsEquality() {
        //given
        int testQuantity = 1;
        List<Boolean> isEqualities = List.of(true);
        SubjectDomain domain = SubjectDomain.values()[1];
        String expected = "Imagine that you are an expert in psychology and social sciences. Write a good excerpt of " +
                "the scientific article of 3000 tokens, which will contain 1 statistical t-tests:\n" +
                "one-tailed t=2.2, df=28, p=0.030;\n" +
                "If you can't, tell me why.";
        List<StatTest> tests = new ArrayList<>();
        tests.add(new StatTest(
                TestType.T, false, 2.2, 28, 0.030, true));
        doReturn(tests).when(generator).generateStatTests(testQuantity);
        doReturn(isEqualities).when(generator).generateEqualities(testQuantity);

        //when
        String actual = builder.buildPrompt(testQuantity, domain);

        //then
        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void createGeneratePromptWithFiveTests() {
        //given
        int testQuantity = 5;
        List<Boolean> isEqualities = List.of(false, false, false, false, false);
        SubjectDomain domain = SubjectDomain.values()[0];
        String expected = "Imagine that you are an expert in medicine and biology. Write a good excerpt of " +
                "the scientific article of 3000 tokens, which will contain 5 statistical t-tests:\n" +
                "two-tailed t=1.2, df=27, p>0.05;\n" +
                "two-tailed t=2.2, df=28, p<0.05;\n" +
                "one-tailed t=3.2, df=29, p<0.01;\n" +
                "two-tailed t=4.2, df=30, p<0.05;\n" +
                "one-tailed t=5.2, df=31, p>0.05;\n" +
                "If you can't, tell me why.";
        List<StatTest> tests = new ArrayList<>();
        tests.add(new StatTest(TestType.T, true, 1.2, 27, 0.02, false));
        tests.add(new StatTest(TestType.T, true, 2.2, 28, 0.03, true));
        tests.add(new StatTest(TestType.T, false, 3.2, 29, 0.005, true));
        tests.add(new StatTest(TestType.T, true, 4.2, 30, 0.07, false));
        tests.add(new StatTest(TestType.T, false, 5.2, 31, 0.06, true));
        doReturn(tests).when(generator).generateStatTests(testQuantity);
        doReturn(isEqualities).when(generator).generateEqualities(testQuantity);

        //when
        String actual = builder.buildPrompt(testQuantity, domain);

        //then
        Assertions.assertThat(actual).isEqualTo(expected);
    }
}
