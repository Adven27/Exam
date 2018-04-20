package com.adven.concordion.extensions.exam.rest;

import com.adven.concordion.extensions.exam.PlaceholdersResolver;
import net.javacrumbs.jsonunit.core.ParametrizedMatcher;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.joda.time.DateTime;
import org.joda.time.base.BaseSingleFieldPeriod;

import static java.lang.Character.isDigit;
import static java.lang.Integer.parseInt;
import static org.joda.time.format.DateTimeFormat.forPattern;

public class DateWithin extends BaseMatcher<Object> implements ParametrizedMatcher {
    private final boolean now;
    private BaseSingleFieldPeriod period;
    private DateTime expected;
    private String pattern;

    private DateWithin(boolean now) {
        this.now = now;
    }

    public static DateWithin param() {
        return new DateWithin(false);
    }

    public static DateWithin now() {
        return new DateWithin(true);
    }

    public boolean matches(Object item) {
        DateTime actual = DateTime.parse((String) item, forPattern(pattern));
        return isBetweenInclusive(expected.minus(period), expected.plus(period), actual);
    }

    boolean isBetweenInclusive(DateTime start, DateTime end, DateTime target) {
        return !target.isBefore(start) && !target.isAfter(end);
    }

    public void describeTo(Description description) {
        description.appendValue(period);
    }

    @Override
    public void describeMismatch(Object item, Description description) {
        description.appendText("The date should be within ").appendValue(period);
    }

    public void setParameter(String param) {
        pattern = param.substring(1, param.indexOf("]"));
        param = param.substring(pattern.length() + 2);
        String within = param.substring(1, param.indexOf("]"));

        if (now) {
            expected = DateTime.now();
        } else {
            param = param.substring(within.length() + 2);
            String date = param.substring(1, param.indexOf("]"));
            expected = DateTime.parse(date, forPattern(pattern));
        }

        int i = 0;
        while (i < within.length() && isDigit(within.charAt(i))) {
            i++;
        }
        this.period = PlaceholdersResolver.INSTANCE.periodBy(
                parseInt(within.substring(0, i)), within.substring(i, within.length()).trim());
    }
}
