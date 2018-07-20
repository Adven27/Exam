package com.adven.concordion.extensions.exam.db.kv;

import com.adven.concordion.extensions.exam.entities.EmptyEntity;
import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.html.ValueBlockParser;
import lombok.val;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.Result;
import org.concordion.api.ResultRecorder;

public final class KVSetCommand extends BaseKeyValueCommand {

    private final KeyValueRepository keyValueRepository;

    public KVSetCommand(final String name, final String tag, final KeyValueRepository keyValueRepository) {
        super(name, tag);
        this.keyValueRepository = keyValueRepository;
    }

    @Override
    public void setUp(final CommandCall commandCall, final Evaluator evaluator, final ResultRecorder resultRecorder) {
        val html = new Html(commandCall.getElement());
        val keyBlock = html.firstOrThrow(KEY);
        val value = new ValueBlockParser("value").parse(html).or(new EmptyEntity());

        html.removeAllChild();

        final String cacheName = html.attr(CACHE);
        val key = keyBlock.text();
        val isSaved = keyValueRepository.save(cacheName, key, value);
        val valueColumn = valueColumn(value.printable());

        final Html keyColumn = keyColumn(key);
        val table = dbTable(cacheName);
        table.childs(keyColumn, valueColumn);
        val info = info("With entries in db");
        html.childs(info).dropAllTo(table);

        if (!isSaved) {
            html.parent().attr("class", "").css("rest-failure bd-callout bd-callout-danger");
            html.text("Failed to send message to kafka");
            resultRecorder.record(Result.EXCEPTION);
        }
    }

}
