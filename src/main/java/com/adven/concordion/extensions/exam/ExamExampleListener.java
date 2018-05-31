package com.adven.concordion.extensions.exam;

import com.adven.concordion.extensions.exam.html.Html;
import org.concordion.api.Element;
import org.concordion.api.ImplementationStatus;
import org.concordion.api.ResultSummary;
import org.concordion.api.listener.ExampleEvent;
import org.concordion.api.listener.ExampleListener;

import static com.adven.concordion.extensions.exam.html.Html.*;
import static org.concordion.api.ImplementationStatus.*;

class ExamExampleListener implements ExampleListener {
    @Override
    public void beforeExample(ExampleEvent event) {
    }

    @Override
    public void afterExample(ExampleEvent event) {
        ResultSummary summary = event.getResultSummary();
        ImplementationStatus status = summary.getImplementationStatus();
        Element card = event.getElement();
        removeConcordionExpectedToFailWarning(card);
        Html stat = Companion.stat().childs(
                Companion.pill(summary.getSuccessCount(), "success"),
                Companion.pill(summary.getIgnoredCount(), "secondary"),
                Companion.pill(summary.getFailureCount(), "warning"),
                Companion.pill(summary.getExceptionCount(), "danger")
        );
        if (status != null) {
            stat.childs(badgeFor(status));
        }
        Companion.footerOf(card).childs(stat);
    }

    private void removeConcordionExpectedToFailWarning(Element card) {
        Element warning = card.getFirstChildElement("p");
        if (warning != null) {
            card.removeChild(warning);
        }
    }

    private Html badgeFor(ImplementationStatus status) {
        switch (status) {
            case EXPECTED_TO_PASS:
                return Companion.pill(EXPECTED_TO_PASS.getTag(), "success");
            case EXPECTED_TO_FAIL:
                return Companion.pill(EXPECTED_TO_FAIL.getTag(), "warning");
            case UNIMPLEMENTED:
                return Companion.pill(UNIMPLEMENTED.getTag(), "primary");
            default:
                throw new UnsupportedOperationException("Unsupported spec implementation status " + status);
        }
    }
}