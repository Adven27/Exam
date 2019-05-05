package com.adven.concordion.extensions.exam.files.commands;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

public enum CheckType {
    NUMBER, STRING, BOOLEAN;

    public boolean suit(String node) {
        switch (this) {
            case NUMBER:
                return isNum(node);
            case BOOLEAN:
                String val = node.toLowerCase();
                return "true".equals(val) || "false".equals(val);
            case STRING:
                return !isNum(node);
            default:
                return false;
        }
    }

    private boolean isNum(String node) {
        return Ints.tryParse(node) != null || Doubles.tryParse(node) != null;
    }
}