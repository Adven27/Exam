package com.adven.concordion.extensions.exam.files.commands;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

public enum CheckType {
    NUMBER, STRING, BOOLEAN;

    public boolean suit(String node) {
        switch (this) {
            case NUMBER:
                return Ints.tryParse(node) != null || Doubles.tryParse(node) != null;
            case BOOLEAN:
                String val = node.toLowerCase();
                return "true".equals(val) || "false".equals(val);
            case STRING:
                return node != null;
            default:
                return false;
        }
    }
}