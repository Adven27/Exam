package io.github.adven27.concordion.extensions.exam.core.utils;

import net.javacrumbs.jsonunit.core.ParametrizedMatcher;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.DifferenceEvaluator;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.xmlunit.diff.ComparisonResult.EQUAL;

public class PlaceholderSupportDiffEvaluator implements DifferenceEvaluator {
    private static final String ANY_NUMBER = "${xml-unit.any-number}";
    private static final String ANY_BOOLEAN = "${xml-unit.any-boolean}";
    private static final String ANY_STRING = "${xml-unit.any-string}";
    private static final String IGNORE_PLACEHOLDER = "${xml-unit.ignore}";
    private static final String REGEX_PLACEHOLDER = "${xml-unit.regex}";
    private static final Pattern MATCHER_PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{xml-unit.matches:(.+)\\}(.*)");
    private final Map<String, org.hamcrest.Matcher<?>> matchers;

    public PlaceholderSupportDiffEvaluator(Map<String, org.hamcrest.Matcher<?>> matchers) {
        this.matchers = matchers;
    }

    @Override
    public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
        if (outcome == EQUAL) {
            return outcome;
        }
        final Node expected = comparison.getControlDetails().getTarget();
        final Node actual = comparison.getTestDetails().getTarget();
        if (expected instanceof Text && actual instanceof Text || expected instanceof Attr && actual instanceof Attr) {
            if (check(expected.getTextContent(), actual.getTextContent())) {
                return EQUAL;
            }
        }
        return outcome;
    }

    private boolean check(String expected, String actual) {
        if (ANY_NUMBER.equals(expected)) {
            return CheckType.NUMBER.suit(actual);
        }
        if (ANY_BOOLEAN.equals(expected)) {
            return CheckType.BOOLEAN.suit(actual);
        }
        if (ANY_STRING.equals(expected)) {
            return CheckType.STRING.suit(actual);
        }
        Matcher patternMatcher = MATCHER_PLACEHOLDER_PATTERN.matcher(expected);
        if (patternMatcher.matches()) {
            String matcherName = patternMatcher.group(1);
            org.hamcrest.Matcher<?> matcher = matchers.get(matcherName);
            if (matcher != null) {
                if (matcher instanceof ParametrizedMatcher) {
                    ((ParametrizedMatcher) matcher).setParameter(patternMatcher.group(2));
                }
                return matcher.matches(actual);
            }
            throw new MatcherNotFound("Matcher \"" + matcherName + "\" not found.");
        }
        if (regex(expected)) {
            return actual.matches(regexPattern(expected));
        }
        return IGNORE_PLACEHOLDER.equals(expected);
    }

    private String regexPattern(String val) {
        return val.substring(REGEX_PLACEHOLDER.length());
    }

    private boolean regex(String val) {
        return val.startsWith(REGEX_PLACEHOLDER);
    }

    private static class MatcherNotFound extends RuntimeException {
        MatcherNotFound(String s) {
            super(s);
        }
    }
}