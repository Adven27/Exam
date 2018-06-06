package com.adven.concordion.extensions.exam.kafka;

import com.adven.concordion.extensions.exam.html.Html;
import com.google.common.base.Optional;

public interface HtmlBlockParser<T> {

    Optional<T> parse(final Html html);

}
