package com.adven.concordion.extensions.exam.core.utils;

import com.github.jknack.handlebars.ValueResolver;
import org.concordion.api.Evaluator;

import java.util.*;

public enum EvaluatorValueResolver implements ValueResolver {
    INSTANCE;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Object resolve(final Object context, final String name) {
        Object value = null;
        if (context instanceof Evaluator) {
            value = ((Evaluator) context).getVariable("#" + name);
        }
        return value == null ? UNRESOLVED : value;
    }

    @Override
    public Object resolve(final Object context) {
        if (context instanceof Evaluator) {
            return context;
        }
        return UNRESOLVED;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Set<Map.Entry<String, Object>> propertySet(final Object context) {
        if (context instanceof Evaluator) {
            throw new UnsupportedOperationException();
        }
        return Collections.emptySet();
    }
}

