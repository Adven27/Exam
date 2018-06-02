package com.adven.concordion.extensions.exam.rest;

import com.adven.concordion.extensions.exam.PlaceholdersResolver;
import com.sun.org.apache.xerces.internal.jaxp.datatype.DatatypeFactoryImpl;
import net.javacrumbs.jsonunit.core.ParametrizedMatcher;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.joda.time.DateTime;
import org.joda.time.base.BaseSingleFieldPeriod;

import javax.xml.datatype.XMLGregorianCalendar;

import static java.lang.Character.isDigit;
import static java.lang.Integer.parseInt;

public class XMLDateWithin extends BaseMatcher<Object> implements ParametrizedMatcher {
    private BaseSingleFieldPeriod period;

    public boolean matches(Object item) {
        XMLGregorianCalendar xmlGregorianCal = new DatatypeFactoryImpl().newXMLGregorianCalendar((String) item);
        DateTime actual = new DateTime(xmlGregorianCal.toGregorianCalendar());
        DateTime expected = DateTime.now();
        return isBetweenInclusive(expected.minus(period), expected.plus(period), actual);
    }

    private boolean isBetweenInclusive(DateTime start, DateTime end, DateTime target) {
        return !target.isBefore(start) && !target.isAfter(end);
    }

    public void describeTo(Description description) {
        description.appendValue(period);
    }

    @Override
    public void describeMismatch(Object item, Description description) {
        description.appendText("The date should be within ").appendValue(period);
    }

    public void setParameter(String within) {
        int i = 0;
        while (i < within.length() && isDigit(within.charAt(i))) {
            i++;
        }
        this.period = PlaceholdersResolver.periodBy(
                parseInt(within.substring(0, i)), within.substring(i, within.length()).trim());
    }
}