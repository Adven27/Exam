package com.sberbank.pfm.test.concordion.extensions.exam.rest;

import net.javacrumbs.jsonunit.core.ParametrizedMatcher;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.joda.time.DateTime;

import static org.joda.time.format.DateTimeFormat.forPattern;

public class DateFormatMatcher extends BaseMatcher<Object> implements ParametrizedMatcher {
    private String param;

    public boolean matches(Object item) {
        try {
            DateTime.parse((String) item, forPattern(param));
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void describeTo(Description description) {
        description.appendValue(param);
    }

    @Override
    public void describeMismatch(Object item, Description description) {
        description.appendText("The date is not properly formatted ").appendValue(param);
    }

    public void setParameter(String parameter) {
        this.param = parameter;
    }
}
