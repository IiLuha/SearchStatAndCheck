package com.itdev.statistic;

import com.github.rcaller.rstuff.RCaller;
import com.github.rcaller.rstuff.RCode;
import com.itdev.parser.StatcheckResultParser;

import java.io.IOException;
import java.util.List;

public class RStatcheckCaller {

    private static final String HEAD = "library(\"statcheck\")\nstat <- statcheck(c(";
    private static final String TAIL = "))";
    private static final String TXT = "txt%d";
    private static final String MID_TXT = TXT + ", ";
    private static final String ONE_TEST_CALL = "library(\"statcheck\")\nstat <- statcheck(txt1)";

    private StatcheckResultParser parser;

    public RStatcheckCaller() {
        this(new StatcheckResultParser());
    }

    public RStatcheckCaller(StatcheckResultParser parser) {
        this.parser = parser;
    }

    public List<StatcheckResultDO> callStatcheck(List<String> testLines) {
        return callRStatcheck(testLines);
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

    private List<StatcheckResultDO> callRStatcheck(List<String> testLines) {
        RCaller caller = RCaller.create();
        RCode code = scriptBuild(testLines);

        caller.setRCode(code);
        caller.runAndReturnResult("stat");

        System.out.println("\nStatcheck xml:\n");

//        try {
//            System.out.println(caller.getParser().getXMLFileAsString());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return parser.parseResult(caller);
    }
}
