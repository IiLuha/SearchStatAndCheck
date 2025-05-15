package itdev.statistic;

import com.itdev.enums.TestType;
import com.itdev.statistic.PValueCalculator;
import com.itdev.statistic.RStatsCaller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PValueCalculatorTest {

    private PValueCalculator calc;
    private RStatsCaller caller;

    @BeforeEach
    void prepare() {
        caller = mock(RStatsCaller.class);
        calc = new PValueCalculator(caller);
        calc = spy(calc);
    }

    @Test
    void returnPIfZOneTailed() {
        //given
        TestType type = TestType.Z;
        double testVal = 2.2;
        double expected = 0.05;
        doReturn(expected).when(caller).callRPNorm(testVal);

        //when
        double actual = calc.calculatePValue(type, testVal, false);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void returnDoubledPIfZTwoTailed() {
        //given
        TestType type = TestType.Z;
        double testVal = 2.2;
        double expected = 0.05;
        doReturn(expected).when(caller).callRPNorm(testVal);
        expected *= 2;

        //when
        double actual = calc.calculatePValue(type, testVal, true);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void throwIfTestTypeNotZWithoutDfs() {
        //given
        double testVal = 2.2;

        //when

        //then
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                calc.calculatePValue(TestType.T, testVal, true));
        Assertions.assertThrows(IllegalArgumentException.class,() ->
                calc.calculatePValue(TestType.F, testVal, true));
        Assertions.assertThrows(IllegalArgumentException.class,() ->
                calc.calculatePValue(TestType.Q, testVal, true));
        Assertions.assertThrows(IllegalArgumentException.class,() ->
                calc.calculatePValue(TestType.R, testVal, true));
        Assertions.assertThrows(IllegalArgumentException.class,() ->
                calc.calculatePValue(TestType.CHI_2, testVal, true));
    }

    @Test
    void returnPIfROneTailed() {
        //given
        TestType type = TestType.R;
        double testVal = 2.2;
        int df = 28;
        double expected = 0.05;
        doReturn(expected).when(caller).callRPT(anyDouble(), anyInt());

        //when
        double actual = calc.calculatePValue(type, testVal, df, false);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void returnDoubledPIfRTwoTailed() {
        //given
        TestType type = TestType.R;
        double testVal = 2.2;
        int df = 28;
        double expected = 0.05;
        doReturn(expected).when(caller).callRPT(anyDouble(), anyInt());
        expected *= 2;

        //when
        double actual = calc.calculatePValue(type, testVal, df, true);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void returnPIfTOneTailed() {
        //given
        TestType type = TestType.T;
        double testVal = 2.2;
        int df = 28;
        double expected = 0.05;
        doReturn(expected).when(caller).callRPT(anyDouble(), anyInt());

        //when
        double actual = calc.calculatePValue(type, testVal, df, false);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void returnDoubledPIfTTwoTailed() {
        //given
        TestType type = TestType.T;
        double testVal = 2.2;
        int df = 28;
        double expected = 0.05;
        doReturn(expected).when(caller).callRPT(anyDouble(), anyInt());
        expected *= 2;

        //when
        double actual = calc.calculatePValue(type, testVal, df, true);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void returnPIfChi() {
        //given
        TestType type = TestType.CHI_2;
        double testVal = 2.2;
        int df = 28;
        double expected = 0.05;
        doReturn(expected).when(caller).callRPChiSq(testVal, df);

        //when
        double actual = calc.calculatePValue(type, testVal, df, false);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void returnPIfQ() {
        //given
        TestType type = TestType.Q;
        double testVal = 2.2;
        int df = 28;
        double expected = 0.05;
        doReturn(expected).when(caller).callRPChiSq(testVal, df);

        //when
        double actual = calc.calculatePValue(type, testVal, df, false);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void throwIfTestTypeIsZ() {
        //given
        TestType type = TestType.Z;
        double testVal = 2.2;
        int df = 28;

        //when
        Executable actual = () -> calc.calculatePValue(type, testVal, df, true);

        //then
        Assertions.assertThrows(IllegalArgumentException.class, actual);
    }

    @Test
    void throwIfTestTypeIsF() {
        //given
        TestType type = TestType.F;
        double testVal = 2.2;
        int df = 28;

        //when
        Executable actual = () -> calc.calculatePValue(type, testVal, df, true);

        //then
        Assertions.assertThrows(IllegalArgumentException.class, actual);
    }

    @Test
    void returnPIfF() {
        //given
        TestType type = TestType.F;
        double testVal = 2.2;
        int df1 = 3;
        int df2 = 48;
        double expected = 0.05;
        doReturn(expected).when(caller).callRPF(testVal, df1, df2);

        //when
        double actual = calc.calculatePValue(type, testVal, df1, df2);

        //then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void throwIfTestTypeNotFAndTwoDf() {
        //given
        double testVal = 2.2;
        int df1 = 3;
        int df2 = 48;

        //when

        //then
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> calc.calculatePValue(TestType.T, testVal, df1, df2));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> calc.calculatePValue(TestType.R, testVal, df1, df2));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> calc.calculatePValue(TestType.Z, testVal, df1, df2));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> calc.calculatePValue(TestType.CHI_2, testVal, df1, df2));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> calc.calculatePValue(TestType.Q, testVal, df1, df2));
    }
}
