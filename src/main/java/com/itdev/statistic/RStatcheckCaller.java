package com.itdev.statistic;

import com.github.rcaller.rstuff.RCaller;
import com.github.rcaller.rstuff.RCode;

import java.io.IOException;
import java.util.List;

public class RStatcheckCaller {

    private static final String HEAD = "library(\"statcheck\")\nstat <- statcheck(c(";
    private static final String TAIL = "))";
    private static final String MID_TXT = "txt%d, ";
    private static final String TXT = "txt%d";

    public String[] callStatcheck(List<String> testLines) {
        return scriptBuild(testLines);
    }

    private String[] scriptBuild(List<String> testLines) {
        RCode rCode = RCode.create();

        StringBuilder code = new StringBuilder(HEAD);
        int testQuantity = testLines.size();
        for (int i = 0; i < testQuantity; i++) {
            rCode.addString(String.format(TXT, i + 1), testLines.get(i));
            code.append(String.format(i == testQuantity - 1 ? TXT : MID_TXT, i + 1));
        }
        code.append(TAIL);
        rCode.addRCode(code.toString());
        return callR(rCode);
    }

    private String[] callR(RCode code) {
        RCaller caller = RCaller.create();

        caller.setRCode(code);
        caller.runAndReturnResult("stat");

        try {
            return new String[]{caller.getParser().getXMLFileAsString()};
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
//        return caller.getParser().getAsStringArray("stat");
    }
}
