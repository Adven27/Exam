package com.adven.concordion.extensions.exam;

import com.adven.concordion.extensions.exam.db.Range;
import org.concordion.api.Evaluator;
import org.joda.time.*;
import org.joda.time.base.BaseSingleFieldPeriod;

import java.util.Date;

import static java.lang.Integer.parseInt;
import static org.joda.time.LocalDateTime.fromDateFields;
import static org.joda.time.LocalDateTime.now;
import static org.joda.time.format.DateTimeFormat.forPattern;

public class PlaceholdersResolver {
    private static final String PREFIX_JSON_UNIT_ALIAS = "!{";
    private static final String PREFIX_EXAM = "${exam.";
    private static final String PREFIX_VAR = "${var.";
    private static final char POSTFIX = '}';

    public static String resolveJson(String body, Evaluator eval) {
        return resolve(body, "json", eval);
    }

    public static String resolveXml(String body, Evaluator eval) {
        return resolve(body, "xml", eval);
    }

    private static String resolve(String body, String type, Evaluator eval) {
        return resolveAliases(
                type,
                resolveExamCommands(
                        resolveVars(body, eval)
                )
        );
    }

    private static String resolveVars(String body, Evaluator eval) {
        while (body.contains(PREFIX_VAR)) {
            String original = body;
            String var = extractVarFrom(original, "var");
            body = original.replace(PREFIX_VAR + var + POSTFIX, getObject(eval, var).toString());
        }
        return body;
    }

    private static Object getObject(Evaluator eval, String var) {
        Object variable = eval.getVariable("#" + var);
        return variable == null ? eval.evaluate(var.contains(".") ? "#" + var : var) : variable;
    }

    private static String resolveExamCommands(String body) {
        while (body.contains(PREFIX_EXAM)) {
            String original = body;
            String var = extractVarFrom(original, "exam");
            if (var.contains(":")) {
                String[] varAndFormat = var.split(":", 2);
                String date = forPattern(varAndFormat[1]).print(fromDateFields((Date) constants(varAndFormat[0])));
                original = original.replace(PREFIX_EXAM + var + POSTFIX, date);
            } else {
                original = original.replace(PREFIX_EXAM + var + POSTFIX, constants(var).toString());
            }
            body = original;
        }
        return body;
    }

    private static String resolveAliases(String type, String body) {
        while (body.contains(PREFIX_JSON_UNIT_ALIAS)) {
            String original = body;
            String alias = extractFromAlias(original);
            body = original.replace(PREFIX_JSON_UNIT_ALIAS + alias + POSTFIX, toPlaceholder(alias, type));
        }
        return body;
    }

    private static String toPlaceholder(String alias, String type) {
        String result;
        switch (alias.toLowerCase()) {
            case "any-string":
            case "string":
            case "str":
                result = "${" + type + "-unit.any-string}";
                break;
            case "any-number":
            case "number":
            case "num":
                result = "${" + type + "-unit.any-number}";
                break;
            case "any-boolean":
            case "boolean":
            case "bool":
                result = "${" + type + "-unit.any-boolean}";
                break;
            case "regexp":
            case "regex":
                result = "${" + type + "-unit.regex}";
                break;
            case "ignored":
            case "ignore":
                result = "${" + type + "-unit.ignore}";
                break;
            default:
                result = String.format("${" + type + "-unit.matches:%s}%s", (Object[]) alias.split(" "));
        }
        return result;
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
            p = isValue(parts[0])
                    ? p.plus(periodBy(parseInt(parts[0]), parts[1]))
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
        if (placeholder.contains(PREFIX_VAR)) {
            return getObject(evaluator, extractVarFrom(placeholder, "var"));
        } else if (placeholder.contains(PREFIX_EXAM)) {
            return constants(extractVarFrom(placeholder, "exam"));
        } else if (Range.isRange(placeholder)) {
            return Range.from(placeholder);
        }
        return placeholder;
    }

    private static String extractVarFrom(String placeholder, final String namespace) {
        String s = placeholder.substring(placeholder.indexOf("${" + namespace + "."));
        return s.substring(s.indexOf('.') + 1, s.indexOf(POSTFIX));
    }

    private static String extractFromAlias(String placeholder) {
        String s = placeholder.substring(placeholder.indexOf(PREFIX_JSON_UNIT_ALIAS));
        return s.substring(2, s.indexOf(POSTFIX));
    }
}