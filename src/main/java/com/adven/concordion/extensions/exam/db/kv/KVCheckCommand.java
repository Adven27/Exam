package com.adven.concordion.extensions.exam.db.kv;

import com.adven.concordion.extensions.exam.db.DbResultRenderer;
import com.adven.concordion.extensions.exam.entities.EmptyEntity;
import com.adven.concordion.extensions.exam.html.Html;
import com.adven.concordion.extensions.exam.html.ValueBlockParser;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.concordion.api.CommandCall;
import org.concordion.api.Evaluator;
import org.concordion.api.ResultRecorder;

@Slf4j
public final class KVCheckCommand extends BaseKeyValueCommand {

    private final KeyValueRepository keyValueRepository;
    private final Announcer announcer;

    public KVCheckCommand(final String name, final String tag, final KeyValueRepository keyValueRepository) {
        super(name, tag);
        this.keyValueRepository = keyValueRepository;
        announcer = new Announcer(new DbResultRenderer());
    }

    @Override
    public void verify(final CommandCall commandCall, final Evaluator evaluator, final ResultRecorder resultRecorder) {
        val html = new Html(commandCall.getElement());
        val keyBlock = html.firstOrThrow(KEY);
        val value = new ValueBlockParser("value").parse(html).or(new EmptyEntity());

        html.removeAllChild();

        val cacheName = html.attr(CACHE);
        val key = keyBlock.text();
        val actual = keyValueRepository.findOne(cacheName, key);
        val expectedString = value.printable();
        val valueColumn = valueColumn(expectedString);

        val keyColumn = keyColumn(key);
        val table = dbTable(cacheName);
        table.childs(keyColumn, valueColumn);
        val info = info("Expected entry");
        html.childs(info).dropAllTo(table);

        if (actual.isPresent()) {
            announcer.success(resultRecorder, keyColumn.el());
            val actualValue = actual.get();
            if (value.isEqualTo(actualValue)) {
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
