package com.adven.concordion.extensions.exam;

import com.adven.concordion.extensions.exam.html.Html;
import org.concordion.api.Element;
import org.concordion.api.ImplementationStatus;
import org.concordion.api.ResultSummary;
import org.concordion.api.listener.ExampleEvent;
import org.concordion.api.listener.ExampleListener;

import static com.adven.concordion.extensions.exam.html.HtmlBuilder.*;
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
        Html stat = stat().childs(
                pill(summary.getSuccessCount(), "success"),
                pill(summary.getIgnoredCount(), "secondary"),
                pill(summary.getFailureCount(), "warning"),
                pill(summary.getExceptionCount(), "danger")
        );
        if (status != null) {
            stat.childs(badgeFor(status));
        }
        footerOf(card).childs(stat);
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
                return pill(EXPECTED_TO_PASS.getTag(), "success");
            case EXPECTED_TO_FAIL:
                return pill(EXPECTED_TO_FAIL.getTag(), "warning");
            case UNIMPLEMENTED:
                return pill(UNIMPLEMENTED.getTag(), "primary");
            default:
                throw new UnsupportedOperationException("Unsupported spec implementation status " + status);
        }
    }
}