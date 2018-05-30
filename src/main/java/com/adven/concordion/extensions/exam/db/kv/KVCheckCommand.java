package com.adven.concordion.extensions.exam.db.kv;

import com.adven.concordion.extensions.exam.db.DbResultRenderer;
import com.adven.concordion.extensions.exam.html.Html;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.javacrumbs.jsonunit.JsonAssert;
import net.javacrumbs.jsonunit.core.Configuration;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

/**
 * @author Ruslan Ustits
 */
@Slf4j
public final class KVCheckCommand extends BaseKeyValueCommand {

    private final KeyValueRepository keyValueRepository;
    private final Announcer announcer;
    private final Configuration jsonCfg;

    public KVCheckCommand(final String name, final String tag, final KeyValueRepository keyValueRepository,
                          final Configuration jsonCfg) {
        super(name, tag);
        this.keyValueRepository = keyValueRepository;
        this.jsonCfg = jsonCfg;
        announcer = new Announcer(new DbResultRenderer());
    }

    @Override
    public void verify(final CommandCall commandCall, final Evaluator evaluator, final ResultRecorder resultRecorder) {
        val html = new Html(commandCall.getElement());
        val keyBlock = html.first("key");
        val valueBlock = html.first("value");

        html.removeAllChild();

        val protobufBlock = valueBlock.first("protobuf");
        final String expectedString;
        if (protobufBlock != null) {
            expectedString = protobufBlock.text();
        } else {
            expectedString = valueBlock.text();
        }

        val key = keyBlock.text();
        final Html keyColumn = keyColumn(key);
        final Html valueColumn = valueColumn(expectedString);

        final String cacheName = html.attr("cache");
        val table = dbTable(cacheName);
        table.childs(keyColumn, valueColumn);

        val info = info("Expected entry");
        html.childs(info)
                .dropAllTo(table);

        val actual = keyValueRepository.findOne(key);
        announcer.success(resultRecorder, keyColumn.el());
        if (actual.isPresent()) {
            final String actualValue = actual.get();
            val valuesAreEqual = valuesAreEqual(expectedString, actualValue);
            if (valuesAreEqual) {
                announcer.success(resultRecorder, valueColumn.el());
            } else {
                announcer.failure(resultRecorder, valueColumn.el(), actualValue, expectedString);
            }
        } else {
            announcer.failure(resultRecorder, valueColumn.el(), "", expectedString);
            html.text("Failed to find any entry for key=" + key);
        }
    }

    private boolean valuesAreEqual(@NonNull final String first, @NonNull final String second) {
        boolean result;
        try {
            JsonAssert.assertJsonEquals(first, second, jsonCfg);
            result = true;
        } catch (AssertionError | Exception e) {
            log.warn("Failed to assert values=[{}, {}]", first, second, e);
            result = false;
        }
        return result;
    }

}
