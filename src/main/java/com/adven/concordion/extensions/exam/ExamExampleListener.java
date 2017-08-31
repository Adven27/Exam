package com.adven.concordion.extensions.exam;

import org.concordion.api.ImplementationStatus;
import org.concordion.api.ResultSummary;
import org.concordion.api.listener.ExampleEvent;
import org.concordion.api.listener.ExampleListener;

import static com.adven.concordion.extensions.exam.html.Html.badge;
import static org.concordion.api.ImplementationStatus.EXPECTED_TO_FAIL;
import static org.concordion.api.ImplementationStatus.EXPECTED_TO_PASS;
import static org.concordion.api.ImplementationStatus.UNIMPLEMENTED;

class ExamExampleListener implements ExampleListener {
    @Override
    public void beforeExample(ExampleEvent event) {

    }

    @Override
    public void afterExample(ExampleEvent event) {
        ResultSummary resultSummary = event.getResultSummary();
        ImplementationStatus status = resultSummary.getImplementationStatus();
        org.concordion.api.Element header = event.getElement().getFirstChildElement("div").getFirstChildElement("a");
        if (header != null) {
            if (status != null) {
                header.appendChild(badgeFor(status));
            } else {
                if (resultSummary.hasExceptions()) {
                    header.appendChild(badge("Fail", "danger").el());
                } else {
                    header.appendChild(badge("Success", "success").el());
                }
            }
        }
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
