package com.adven.concordion.extensions.exam.html;

import org.concordion.api.Evaluator;

import java.util.ArrayList;
import java.util.List;

import static com.adven.concordion.extensions.exam.PlaceholdersResolver.resolveToObj;
import static com.google.common.base.Strings.isNullOrEmpty;

public class RowParser {
    private final String tag;
    private final Evaluator eval;
    private final int ignoreBefore;
    private final int ignoreAfter;
    private final String separator;
    private final Html el;

    public RowParser(Html el, String tag, Evaluator eval) {
        String ignoreBeforeStr = el.takeAwayAttr("ignoreRowsBefore", eval);
        String ignoreAfterStr = el.takeAwayAttr("ignoreRowsAfter", eval);
        ignoreBefore = ignoreBeforeStr != null ? Integer.parseInt(ignoreBeforeStr) : 1;
        ignoreAfter = ignoreAfterStr != null ? Integer.parseInt(ignoreAfterStr) : 0;
        this.tag = tag;
        this.eval = eval;
        separator = el.takeAwayAttr("separator", ",");
        this.el = el;
    }

    public List<List<Object>> parse() {
        List<List<Object>> result = new ArrayList<>();
        int i = 1;
        for (Html r : el.childs()) {
            if (tag.equals(r.localName())) {
                if (i >= ignoreBefore && (ignoreAfter == 0 || i <= ignoreAfter)) {
                    result.add(parseValues(eval, r.text(), separator));
                }
                el.remove(r);
                i++;
            }
        }
        return result;
    }

    private List<Object> parseValues(Evaluator eval, String text, String separator) {
        List<Object> values = new ArrayList<>();
        if (isNullOrEmpty(text)) {
            values.add(text);
        } else {
            for (String val : text.split("[" + separator + "]", -1)) {
                values.add(resolveToObj(preservePaddingInside("'", val.trim()), eval));
            }
        }
        return values;
    }

    private String preservePaddingInside(String bound, String val) {
        if (val.startsWith(bound) && val.endsWith(bound)) {
            val = val.substring(1, val.length() - 1);
        }
        return val;
    }
}