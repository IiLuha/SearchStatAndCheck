package com.itdev;

import com.itdev.http.HttpToLLM;
import com.itdev.enums.ModelLLM;
import com.itdev.parser.ResponseParser;
import com.itdev.prompt.SearchPrompt;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static com.itdev.enums.ModelLLM.*;

public class ApplicationRunner {

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
            HttpToLLM http = new HttpToLLM();
            switch (model) {
                case DEEPSEEK -> response = http.deepseekChatCompletion(SearchPrompt.PROMPT_ALL + text);
                case LLAMA -> response = http.llamaGenerateCompletion(SearchPrompt.PROMPT_ALL + text);
                default -> response = "Unknown model";
            }
            text = ResponseParser.extractGeneratedText(response, model) + "\n end";

            System.out.println("Response:\n" + text);
        }

    }
}
