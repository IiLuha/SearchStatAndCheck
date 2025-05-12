package com.itdev;

import com.itdev.http.HttpToLLM;
import com.itdev.models.ModelLLM;
import com.itdev.parser.ResponseParser;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static com.itdev.models.ModelLLM.*;

public class ApplicationRunner {

    private static final String PROMPT_CHI = """
            Find the chi-square statistical tests in the text after the colon and present them in APA format (like "χ2 (763, N = 292) = 1467.59, p < .001") (do not write anything else):
            """;

    private static final String PROMPT_ALL = """
            Identify and extract all instances of statistical tests reported in the following article. The tests may include, but are not limited to: t-tests, F-tests, correlations, z-tests, chi-square tests, and Q-tests. For each test found, provide the type of test (e.g., 'paired t-test'), the test statistic value, Degrees of freedom (if reported), the p-value. Present them in APA format (chi-square tests like "χ2 (763, N = 292) = 1467.59, p < .001" or paired t-test like "paired t(28) = 2.20 , p = 0.03"). Do not write anything else. If there are no tests in the article, then display "no tests".
            Article:
            """;

    public static void main(String[] args) throws IOException {
        try (
                PDDocument document = Loader.loadPDF(new File("articles/1article.pdf"));
                FileOutputStream stream = new FileOutputStream(Path.of("output.txt").toFile())
        ) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            //System.out.println(text);
            ModelLLM model = DEEPSEEK;
            String response;
            switch (model) {
                case DEEPSEEK -> response = HttpToLLM.deepseekChatCompletion(PROMPT_ALL + text);
                case LLAMA -> response = HttpToLLM.llamaGenerateCompletion(PROMPT_ALL + text);
                default -> response = "Unknown model";
            }
            text = ResponseParser.extractGeneratedText(response, model) + "/n end";

            System.out.println("Response:\n" + text);
        }

    }
}
