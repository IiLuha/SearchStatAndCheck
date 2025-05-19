package com.itdev.prompt;

public class SearchPrompt {

    public static final String PROMPT_CHI = """
            Find the chi-square statistical tests in the text after the colon and present them in APA format (like "Chi2 (763, N = 292) = 1467.59, p < .001") (do not write anything else):
            """;

    public static final String PROMPT_ALL = """
            Identify and extract all instances of statistical tests reported in the following article. The tests may include, but are not limited to: t-tests, F-tests, correlations, z-tests,  chi-square tests, and Q-tests, not other. For each test found, provide: The type of test (e.g., 'one-tailed t-test'), The test statistic value (e.g., t = 2.15, F = 4.32, r = 0.56, χ² = 3.84), Degrees of freedom (if reported, e.g., df = 18 or df = (2, 24)), The p-value (if reported, e.g., p = 0.032 or p < 0.05). Present them ONLY in APA format (two-tailed chi-square tests like "two-tailed χ2 (763, N = 292) = 1467.59, p < .001" or one-tailed t-test like "one-tailed t(28) = 2.20, p = 0.03"). Do not write anything else. If there are no tests in the article, then display "no tests".
            Article:
            """;
}
