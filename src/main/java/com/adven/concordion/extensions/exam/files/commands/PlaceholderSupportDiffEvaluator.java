package com.adven.concordion.extensions.exam.files.commands;

import net.javacrumbs.jsonunit.core.Configuration;
import net.javacrumbs.jsonunit.core.ParametrizedMatcher;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.DifferenceEvaluator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.adven.concordion.extensions.exam.files.commands.CheckType.*;
import static org.xmlunit.diff.ComparisonResult.EQUAL;

class PlaceholderSupportDiffEvaluator implements DifferenceEvaluator {
    private static final String ANY_NUMBER = "${xml-unit.any-number}";
    private static final String ANY_BOOLEAN = "${xml-unit.any-boolean}";
    private static final String ANY_STRING = "${xml-unit.any-string}";
    private static final String IGNORE_PLACEHOLDER = "${xml-unit.ignore}";
    private static final String REGEX_PLACEHOLDER = "${xml-unit.regex}";
    private static final Pattern MATCHER_PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{xml-unit.matches:(.+)\\}(.*)");
    //FIXME temporary(HA!) reuse json-unit cfg for matchers retrieving
    private final Configuration configuration;

    public PlaceholderSupportDiffEvaluator(Configuration jsonUnitCfg) {
        this.configuration = jsonUnitCfg;
    }

    @Override
    public ComparisonResult evaluate(Comparison comparison, ComparisonResult outcome) {
        if (outcome == EQUAL) {
            return outcome;
        }
        final Node expectedNode = comparison.getControlDetails().getTarget();
        final Node actualNode = comparison.getTestDetails().getTarget();
        if (expectedNode instanceof Text && actualNode instanceof Text) {
            final String expected = expectedNode.getTextContent();
            final String actual = actualNode.getTextContent();

            if (check(expected, actual)) {
                return EQUAL;
            }
        }
        return outcome;
    }

    private boolean check(String expected, String actual) {
        if (ANY_NUMBER.equals(expected)) {
            return NUMBER.suit(actual);
        }
        if (ANY_BOOLEAN.equals(expected)) {
            return BOOLEAN.suit(actual);
        }
        if (ANY_STRING.equals(expected)) {
            return STRING.suit(actual);
        }
        Matcher patternMatcher = MATCHER_PLACEHOLDER_PATTERN.matcher(expected);
        if (patternMatcher.matches()) {
            String matcherName = patternMatcher.group(1);
            org.hamcrest.Matcher<?> matcher = configuration.getMatcher(matcherName);
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