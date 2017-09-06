package com.adven.concordion.extensions.exam;

import com.adven.concordion.extensions.exam.html.Html;
import org.concordion.api.Element;
import org.concordion.api.ImplementationStatus;
import org.concordion.api.ResultSummary;
import org.concordion.api.listener.ExampleEvent;
import org.concordion.api.listener.ExampleListener;

import static com.adven.concordion.extensions.exam.html.Html.badge;
import static com.adven.concordion.extensions.exam.html.Html.pill;
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
        org.concordion.api.Element header = card.getFirstChildElement("div").getFirstChildElement("a");
        if (header != null) {
            if (status != null) {
                header.appendChild(badgeFor(status));
            } else {
                if (summary.hasExceptions()) {
                    header.appendChild(badge("Fail", "danger").el());
                } else {
                    header.appendChild(badge("Success", "success").el());
                }
            }
        }
        new Html(card.getChildElements("div")[2]).childs(
                pill(summary.getSuccessCount(), "success"),
                pill(summary.getIgnoredCount(), "secondary"),
                pill(summary.getFailureCount(), "warning"),
                pill(summary.getExceptionCount(), "danger")
        );
    }

    private org.concordion.api.Element badgeFor(ImplementationStatus status) {
        switch (status) {
            case EXPECTED_TO_PASS:
                return badge(EXPECTED_TO_PASS.getTag(), "info").el();
            case EXPECTED_TO_FAIL:
                return badge(EXPECTED_TO_FAIL.getTag(), "warning").el();
            case UNIMPLEMENTED:
                return badge(UNIMPLEMENTED.getTag(), "default").el();
        }
        throw new UnsupportedOperationException("Unsupported spec implementation status " + status);
    }
}
