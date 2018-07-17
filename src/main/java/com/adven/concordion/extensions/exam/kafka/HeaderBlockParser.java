package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.html.HtmlBlockParser;
import com.google.common.base.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.UnsupportedEncodingException;

@Slf4j
public final class HeaderBlockParser implements HtmlBlockParser<EventHeader> {

    static final String REPLY_TO_TOPIC = "replyToTopic";
    static final String COR_ID = "correlationId";

    @Override
    public Optional<EventHeader> parse(final Html html) {
        val headers = html.first("headers");
        if (headers == null) {
            return Optional.absent();
        }
        val replyToTopic = headers.first(REPLY_TO_TOPIC);
        val correlationId = headers.first(COR_ID);
        return Optional.of(buildHeader(replyToTopic, correlationId));
    }

    private EventHeader buildHeader(final Html replyToTopic, final Html correlationId) {
        return new EventHeader(retrieveTextInBytes(replyToTopic), retrieveTextInBytes(correlationId));
    }

    protected byte[] retrieveTextInBytes(final Html html) {
        try {
            return html == null ? new byte[]{} : html.text().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Bad encoding", e);
        }
        return new byte[]{};
    }

}
