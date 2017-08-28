package com.adven.concordion.extensions.exam;

import org.concordion.api.Evaluator;
import org.joda.time.Period;
import org.junit.Test;

import static com.adven.concordion.extensions.exam.PlaceholdersResolver.resolve;
import static org.hamcrest.Matchers.is;
import static org.joda.time.LocalDateTime.now;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PlaceholdersResolverTest {
    Evaluator eval = mock(Evaluator.class);

    @Test
    public void canUseConcordionVars() throws Exception {
        when(eval.getVariable("#value")).thenReturn(3);

        assertThat(resolve("${var.value}", eval), is("3"));
    }

    @Test
    public void canUseJsonUnitStringAliases() throws Exception {
        String expected = "${json-unit.any-string}";
        assertThat(resolve("!{any-string}", eval), is(expected));
        assertThat(resolve("!{aNy-stRiNG}", eval), is(expected));
        assertThat(resolve("!{string}", eval), is(expected));
        assertThat(resolve("!{str}", eval), is(expected));
    }

    @Test
    public void canUseJsonUnitNumberAliases() throws Exception {
        String expected = "${json-unit.any-number}";
        assertThat(resolve("!{any-number}", eval), is(expected));
        assertThat(resolve("!{aNy-nuMBeR}", eval), is(expected));
        assertThat(resolve("!{number}", eval), is(expected));
        assertThat(resolve("!{num}", eval), is(expected));
    }

    @Test
    public void canUseJsonUnitBoolAliases() throws Exception {
        String expected = "${json-unit.any-boolean}";
        assertThat(resolve("!{any-boolean}", eval), is(expected));
        assertThat(resolve("!{aNy-bOOlean}", eval), is(expected));
        assertThat(resolve("!{boolean}", eval), is(expected));
        assertThat(resolve("!{bool}", eval), is(expected));
    }

    @Test
    public void canUseJsonUnitMatcherAliases() throws Exception {
        assertThat(resolve("!{formattedAs dd.MM.yyyy}", eval),
                is("${json-unit.matches:formattedAs}dd.MM.yyyy"));
        assertThat(resolve("!{formattedAndWithin [yyyy-MM-dd][1d][1951-05-13]}", eval),
                is("${json-unit.matches:formattedAndWithin}[yyyy-MM-dd][1d][1951-05-13]"));
    }

    @Test
    public void canAddSimplePeriodToNow() throws Exception {
        String expected = now().plusDays(1).toString("dd.MM.yyyy");

        assertThat(resolve("${exam.now+[1 d]:dd.MM.yyyy}", eval), is(expected));
        assertThat(resolve("${exam.now+[1 day]:dd.MM.yyyy}", eval), is(expected));
        assertThat(resolve("${exam.now+[day 1]:dd.MM.yyyy}", eval), is(expected));
        assertThat(resolve("${exam.now+[1 days]:dd.MM.yyyy}", eval), is(expected));
    }

    @Test
    public void canAddCompositePeriodsToNow() throws Exception {
        assertThat(
                resolve(
                        "${exam.now+[1 day, 1 month]:dd.MM.yyyy}", eval
                ), is(
                        now().plus(new Period().
                                plusDays(1).plusMonths(1)).toString("dd.MM.yyyy")
                )
        );
        assertThat(
                resolve(
                        "${exam.now+[days 3, months 3, 3 years]:dd.MM.yyyy}", eval
                ), is(
                        now().plus(new Period().
                                plusDays(3).plusMonths(3).plusYears(3)).toString("dd.MM.yyyy")
                )
        );
        assertThat(
                resolve(
                        "${exam.now+[4 d, 4 M, y 4, 4 hours]:dd.MM.yyyy'T'hh}", eval
                ), is(
                        now().plus(new Period().
                                plusDays(4).plusMonths(4).plusYears(4).plusHours(4)).toString("dd.MM.yyyy'T'hh")
                )
        );
    }

    @Test
    public void canSubtractCompositePeriodsFromNow() throws Exception {
        assertThat(
                resolve(
                        "${exam.now-[1 day, 1 month]:dd.MM.yyyy}", eval
                ), is(
                        now().minusDays(1).minusMonths(1).toString("dd.MM.yyyy")
                )
        );
        assertThat(
                resolve(
                        "${exam.now-[days 3, months 3, 3 years]:dd.MM.yyyy}", eval
                ), is(
                        now().minusDays(3).minusMonths(3).minusYears(3).toString("dd.MM.yyyy")
                )
        );
        assertThat(
                resolve(
                        "${exam.now-[4 d, 4 M, y 4, 4 hours]:dd.MM.yyyy'T'hh}", eval
                ), is(
                        now().minusDays(4).minusMonths(4).minusYears(4).minusHours(4).toString("dd.MM.yyyy'T'hh")
                )
        );
    }
}