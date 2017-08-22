package com.sberbank.pfm.test.concordion.extensions.exam;

import org.concordion.api.Evaluator;
import org.joda.time.*;
import org.joda.time.base.BaseSingleFieldPeriod;

import java.util.Date;

import static java.lang.Integer.parseInt;
import static org.joda.time.LocalDateTime.fromDateFields;
import static org.joda.time.LocalDateTime.now;
import static org.joda.time.format.DateTimeFormat.forPattern;

public class PlaceholdersResolver {

    public static String resolve(String body, Evaluator eval) {
        while (body.contains("${var.")) {
            String original = body;
            String var = extractVarFrom(original, "var");
            Object variable = eval.getVariable("#" + var);

            original = original.replace("${var." + var + "}",
                    (variable == null ? eval.evaluate(var.contains(".") ? "#" + var : var) : variable).toString());
            body = original;
        }
        return resolve(body);
    }

    public static String resolve(String body) {
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
        if (var.startsWith("date(")) {
            String date = var.substring("date(".length(), var.indexOf(")"));
            return LocalDateTime.parse(date, forPattern("dd.MM.yyyy")).toDate();
        } else if (var.startsWith("now+")) {
            return now().plus(parsePeriod(var)).toDate();
        } else if (var.startsWith("now-")) {
            return now().minus(parsePeriod(var)).toDate();
        }
        switch (var) {
            case "yesterday":
                return now().minusDays(1).toDate();
            case "today":
            case "now":
                return now().toDate();
            case "tomorrow":
                return now().plusDays(1).toDate();
            default:
                return null;
        }
    }

    private static Period parsePeriod(String var) {
        Period p = Period.ZERO;
        String[] periods = var.substring(5, var.indexOf("]")).split(",");
        for (String period : periods) {
            String[] parts = period.trim().split(" ");
            p = isValue(parts[0]) ? p.plus(periodBy(parseInt(parts[0]), parts[1]))
                                  : p.plus(periodBy(parseInt(parts[1]), parts[0]));
        }
        return p;
    }

    public static BaseSingleFieldPeriod periodBy(int val, String type) {
        switch (type) {
            case "d":
            case "day":
            case "days":
                return Days.days(val);
            case "M":
            case "month":
            case "months":
                return Months.months(val);
            case "y":
            case "year":
            case "years":
                return Years.years(val);
            case "h":
            case "hour":
            case "hours":
                return Hours.hours(val);
            case "m":
            case "min":
            case "minute":
            case "minutes":
                return Minutes.minutes(val);
            case "s":
            case "sec":
            case "second":
            case "seconds":
                return Seconds.seconds(val);
            default:
                throw new UnsupportedOperationException("Unsupported period type " + type);
        }
    }

    private static boolean isValue(String part) {
        try {
            parseInt(part);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
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