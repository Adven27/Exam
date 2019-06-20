package com.adven.concordion.extensions.exam.ws;

import org.apache.commons.lang3.StringUtils;


public final class StatusBuilder {

    private static final String DEFAULT_PROTOCOL = "HTTP/1.1";
    private static final String DEFAULT_STATUS_CODE = "200";
    private static final String DEFAULT_REASON_PHRASE = "OK";

    private final String protocol;
    private final String status;
    private final String reason;

    public StatusBuilder(final String protocol, final String status, final String reason) {
        this.protocol = protocol == null ? DEFAULT_PROTOCOL : protocol;
        this.status = status == null ? DEFAULT_STATUS_CODE : status;
        this.reason = reason == null ? DEFAULT_REASON_PHRASE : reason;
    }

    public String build() {
        final String raw = StringUtils.join(new String[]{protocol, status, reason}, ' ');
        return clean(raw);
    }

    protected String clean(final String status) {
        return status.replaceAll("\\b\\s*$", "");
    }

}
