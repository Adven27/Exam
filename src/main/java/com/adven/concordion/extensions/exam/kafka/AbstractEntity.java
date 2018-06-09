package com.adven.concordion.extensions.exam.kafka;

import lombok.NonNull;

public abstract class AbstractEntity implements Entity {

    protected final String cleanup(@NonNull final String message) {
        return message.replaceAll("\\s", "");
    }


}
