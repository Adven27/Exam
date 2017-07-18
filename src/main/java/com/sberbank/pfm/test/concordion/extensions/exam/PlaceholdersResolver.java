package com.sberbank.pfm.test.concordion.extensions.exam;

import org.concordion.api.Evaluator;

import java.util.Date;

import static org.joda.time.LocalDateTime.fromDateFields;
import static org.joda.time.LocalDateTime.now;
import static org.joda.time.format.DateTimeFormat.forPattern;

public class PlaceholdersResolver {

    public static String resolve(String body, Evaluator evaluator) {
        while (body.contains("${var.")) {
            String original = body;
            String var = extractVarFrom(original, "var");
            Object variable = evaluator.getVariable("#" + var);

            original = original.replace("${var." + var + "}",
                    (variable == null ? evaluator.evaluate(var) : variable).toString());
            body = original;
        }
        while (body.contains("${exam.")) {
            String original = body;
            String var = extractVarFrom(original, "exam");
            if (var.contains(":")) {
                String[] varAndFormat = var.split(":", 2);
                String date = forPattern(varAndFormat[1]).print(fromDateFields((Date) constants(varAndFormat[0])));
                original = original.replace("${exam." + var + "}", date);
            } else {
                original = original.replace("${exam." + var + "}", constants(var).toString());
            }
            body = original;
        }
        return body;
    }

    private static Object constants(String var) {
        switch (var) {
            case "yesterday": return now().minusDays(1).toDate();
            case "today": case "now": return now().toDate();
            case "tomorrow": return now().plusDays(1).toDate();
            default: return null;
        }
    }

    public static Object resolveToObj(String placeholder, Evaluator evaluator) {
        if (placeholder.contains("${var.")) {
            final String var = extractVarFrom(placeholder, "var");
            Object variable = evaluator.getVariable("#" + var);
            return variable == null ? evaluator.evaluate(var) : variable;
        } else if (placeholder.contains("${exam.")) {
            return constants(extractVarFrom(placeholder, "exam"));
        }
        return placeholder;
    }

    private static String extractVarFrom(String placeholder, final String namespace) {
        String s = placeholder.substring(placeholder.indexOf("${" + namespace + "."));
        return s.substring(s.indexOf('.') + 1, s.indexOf('}'));
    }
}