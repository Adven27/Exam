window.addEventListener('load', ready, false);

function ready() {
    enableCodeMirror('.json:not(.rest-failure)', {name: 'javascript', json: true});
    enableCodeMirrorMerge('.json.rest-failure', {name: 'javascript', json: true});
    enableCodeMirror('.xml:not(.rest-failure)', 'application/xml');
    enableCodeMirrorMerge('.xml.rest-failure', 'application/xml');
}

function unescape(input) {
    var e = document.createElement('div');
    e.innerHTML = input;
    return e.childNodes.length === 0 ? "" : e.childNodes[0].nodeValue;
}

function enableCodeMirror(selector, mode) {
    var value, editor,
        jsons = document.querySelectorAll(selector);

    for (var i = 0; i < jsons.length; i++) {
        value = unescape(jsons[i].innerHTML);
        jsons[i].innerHTML = "";

        editor = CodeMirror(jsons[i], {
            lineNumbers: false,
            mode: mode,
            value: value,
            readOnly: true
        });
        autoFormat(editor);
    }
}

function enableCodeMirrorMerge(selector, mode) {
    var jsons = document.querySelectorAll(selector);
    for (var i = 0; i < jsons.length; i++) {
        mergeView(jsons[i], mode);
    }

}
function mergeView(target, mode) {
    var expectedValue, actualValue,
        expected = target.querySelector('.expected'),
        actual = target.querySelector('.actual');

    expectedValue = unescape(expected.innerHTML);
    actualValue = unescape(actual.innerHTML);

    actual.parentNode.removeChild(actual);
    expected.parentNode.removeChild(expected);
    target.innerHTML = '';

    CodeMirror.MergeView(target, {
        value: expectedValue,
        origLeft: null,
        orig: actualValue,
        lineNumbers: false,
        mode: mode,
        highlightDifferences: true,
        collapseIdentical: false
    });
}

function autoFormat(editor) {
    var range = {from: {line: 0, ch: 0}, to: {line: editor.lineCount(), ch: editor.getValue().length}};
    editor.autoFormatRange(range.from, range.to);
}