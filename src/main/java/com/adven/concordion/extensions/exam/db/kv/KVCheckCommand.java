package com.adven.concordion.extensions.exam.db.kv;

import com.adven.concordion.extensions.exam.db.DbResultRenderer;
import com.adven.concordion.extensions.exam.db.kv.repositories.PlainProcessor;
import com.adven.concordion.extensions.exam.db.kv.repositories.ProtobufProcessor;
import com.adven.concordion.extensions.exam.db.kv.repositories.ValueProcessor;
import com.adven.concordion.extensions.exam.html.Html;
import com.google.common.base.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
        val keyBlock = html.first(KEY);
        val valueBlock = html.first(VALUE);

        html.removeAllChild();

        final String cacheName = html.attr(CACHE);
        val protobufBlock = valueBlock.first(PROTOBUF);
        val key = keyBlock.text();
        final Optional<String> actual;
        final String expectedString;
        final ValueProcessor valueProcessor;
        final Html valueColumn;
        if (protobufBlock != null) {
            valueProcessor = new ProtobufProcessor(jsonCfg);
            expectedString = protobufBlock.text();
            actual = keyValueRepository.findOne(cacheName, key, valueProcessor);
            valueColumn = jsonValueColumn(expectedString);
        } else {
            valueProcessor = new PlainProcessor();
            expectedString = valueBlock.text();
            actual = keyValueRepository.findOne(cacheName, key, valueProcessor);
            valueColumn = valueColumn(expectedString);
        }

        final Html keyColumn = keyColumn(key);
        val table = dbTable(cacheName);
        table.childs(keyColumn, valueColumn);
        val info = info("Expected entry");
        html.childs(info)
                .dropAllTo(table);

        if (actual.isPresent()) {
            announcer.success(resultRecorder, keyColumn.el());
            final String actualValue = actual.get();
            val valuesAreEqual = valueProcessor.verify(expectedString, actualValue);
            if (valuesAreEqual) {
                announcer.success(resultRecorder, valueColumn.el());
            } else {
                announcer.failure(resultRecorder, valueColumn.el(), actualValue, expectedString);
            }
        } else {
            announcer.failure(resultRecorder, keyColumn.el(), "", key);
            announcer.failure(resultRecorder, valueColumn.el(), "", expectedString);
            html.text("Failed to find any entry for key=" + key);
        }
    }

}
