package com.adven.concordion.extensions.exam.db.kv;

import com.adven.concordion.extensions.exam.db.kv.repositories.PlainProcessor;
import com.adven.concordion.extensions.exam.db.kv.repositories.ProtobufProcessor;
import com.adven.concordion.extensions.exam.html.Html;
import lombok.val;
import net.javacrumbs.jsonunit.core.Configuration;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.Result;
import org.concordion.api.ResultRecorder;

/**
 * @author Ruslan Ustits
 */
public final class KVSetCommand extends BaseKeyValueCommand {

    private final KeyValueRepository keyValueRepository;
    private final Configuration jsonCfg;

    public KVSetCommand(final String name, final String tag, final KeyValueRepository keyValueRepository,
                        final Configuration jsonCfg) {
        super(name, tag);
        this.keyValueRepository = keyValueRepository;
        this.jsonCfg = jsonCfg;
    }

    @Override
    public void setUp(final CommandCall commandCall, final Evaluator evaluator, final ResultRecorder resultRecorder) {
        val html = new Html(commandCall.getElement());
        val keyBlock = html.first(KEY);
        val valueBlock = html.first(VALUE);

        html.removeAllChild();

        final String cacheName = html.attr(CACHE);
        val protobufBlock = valueBlock.first(PROTOBUF);
        val key = keyBlock.text();
        final boolean isSaved;
        final Html valueColumn;
        if (protobufBlock != null) {
            val value = protobufBlock.text();
            val className = protobufBlock.attr("class");
            isSaved = keyValueRepository.save(cacheName, key, value, className, new ProtobufProcessor(jsonCfg));
            valueColumn = jsonValueColumn(value);
        } else {
            val value = valueBlock.text();
            isSaved = keyValueRepository.save(cacheName, key,value, "", new PlainProcessor());
            valueColumn = jsonValueColumn(value);
        }

        final Html keyColumn = keyColumn(key);
        val table = dbTable(cacheName);
        table.childs(keyColumn, valueColumn);
        val info = info("With entries in db");
        html.childs(info)
                .dropAllTo(table);

        if (!isSaved) {
            html.parent().attr("class", "")
                    .css("rest-failure bd-callout bd-callout-danger");
            html.text("Failed to send message to kafka");
            resultRecorder.record(Result.EXCEPTION);
        }
    }
}
