package com.sberbank.pfm.test.concordion.extensions.exam;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.joda.time.LocalDateTime.now;
import static org.junit.Assert.assertThat;

public class PlaceholdersResolverTest {

    @Test
    public void canAddSimplePeriodToNow() throws Exception {
        String value1 = "${exam.now+[1 day]:dd.MM.yyyy}";
        String value2 = "${exam.now+[day 1]:dd.MM.yyyy}";
        String value3 = "${exam.now+[1 d]:dd.MM.yyyy}";
        String value4 = "${exam.now+[1 days]:dd.MM.yyyy}";

        String expected = now().plusDays(1).toString("dd.MM.yyyy");

        assertThat(PlaceholdersResolver.resolve(value1), is(expected));
        assertThat(PlaceholdersResolver.resolve(value2), is(expected));
        assertThat(PlaceholdersResolver.resolve(value3), is(expected));
        assertThat(PlaceholdersResolver.resolve(value4), is(expected));
    }

    @Test
    public void canAddCompositePeriodsToNow() throws Exception {
        String dm = "${exam.now+[1 day, 1 month]:dd.MM.yyyy}";
        String dmy = "${exam.now+[days 3, months 3, 3 years]:dd.MM.yyyy}";
        String dmyh = "${exam.now+[4 d, 4 M, y 4, 4 hours]:dd.MM.yyyy'T'hh}";

        assertThat(PlaceholdersResolver.resolve(dm), is(
                now().plusDays(1).plusMonths(1).toString("dd.MM.yyyy")));
        assertThat(PlaceholdersResolver.resolve(dmy), is(
                now().plusDays(3).plusMonths(3).plusYears(3).toString("dd.MM.yyyy")));
        assertThat(PlaceholdersResolver.resolve(dmyh), is(
                now().plusDays(4).plusMonths(4).plusYears(4).plusHours(4).toString("dd.MM.yyyy'T'hh")));
    }

    @Test
    public void canSubtractCompositePeriodsFromNow() throws Exception {
        String dm = "${exam.now-[1 day, 1 month]:dd.MM.yyyy}";
        String dmy = "${exam.now-[days 3, months 3, 3 years]:dd.MM.yyyy}";
        String dmyh = "${exam.now-[4 d, 4 M, y 4, 4 hours]:dd.MM.yyyy'T'hh}";

        assertThat(PlaceholdersResolver.resolve(dm), is(
                now().minusDays(1).minusMonths(1).toString("dd.MM.yyyy")));
        assertThat(PlaceholdersResolver.resolve(dmy), is(
                now().minusDays(3).minusMonths(3).minusYears(3).toString("dd.MM.yyyy")));
        assertThat(PlaceholdersResolver.resolve(dmyh), is(
                now().minusDays(4).minusMonths(4).minusYears(4).minusHours(4).toString("dd.MM.yyyy'T'hh")));
    }
}