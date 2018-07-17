package com.adven.concordion.extensions.exam.html;

import com.google.common.base.Optional;

public interface HtmlBlockParser<T> {

    Optional<T> parse(final Html html);

}
