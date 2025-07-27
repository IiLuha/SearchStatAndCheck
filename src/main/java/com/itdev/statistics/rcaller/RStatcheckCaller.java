package com.itdev.statistics.rcaller;

import com.github.rcaller.exception.ExecutionException;
import com.github.rcaller.rstuff.RCaller;
import com.github.rcaller.rstuff.RCode;
import com.itdev.exception.StatcheckException;
import com.itdev.parser.StatcheckResultParser;
import com.itdev.statistics.StatcheckResultDO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RStatcheckCaller {

    private static final String HEAD = "library(\"statcheck\")\nstat <- statcheck(c(";
    private static final String TAIL = "))";
    private static final String TXT = "txt%d";
    private static final String MID_TXT = TXT + ", ";
    private static final String ONE_TEST_CALL = "library(\"statcheck\")\nstat <- statcheck(txt1)";

    private final StatcheckResultParser parser;

    public List<StatcheckResultDO> callStatcheck(List<String> testLines) throws StatcheckException {
        List<String> editedLines = testLines.stream()
//                .peek(System.out::println)
                .map(test -> test.replace("\\", "\\\\"))
                .map(test -> test.replace("'", "\\'"))
                .map(test -> test.replace("\"", "\\\""))
                .map(test -> test.replace("\n", " "))
                .map(test -> test.replace("\r", ""))
                .map(test -> test.replace("#", ""))
//                .peek(System.out::println)
                .toList();
        return callRStatcheck(editedLines);
    }

    private RCode scriptBuild(List<String> testLines) {
        RCode rCode = RCode.create();

        if (testLines.size() == 1) {
            rCode.addString(String.format(TXT, 1), testLines.get(0));
            rCode.addRCode(ONE_TEST_CALL);
        }

        StringBuilder code = new StringBuilder(HEAD);
        int testQuantity = testLines.size();
        for (int i = 0; i < testQuantity; i++) {
            rCode.addString(String.format(TXT, i + 1), testLines.get(i));
            code.append(String.format(i == testQuantity - 1 ? TXT : MID_TXT, i + 1));
        }
        code.append(TAIL);
        rCode.addRCode(code.toString());
        return rCode;
    }

    private List<StatcheckResultDO> callRStatcheck(List<String> testLines) throws StatcheckException {
        RCaller caller = RCaller.create();
        RCode code = scriptBuild(testLines);

        caller.setRCode(code);
        try {
            caller.runAndReturnResult("stat");
        } catch (ExecutionException e) {
            throw new StatcheckException(e.getMessage());
        }

        return parser.parseResult(caller);
    }
}
