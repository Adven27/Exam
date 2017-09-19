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

    var i, value, editor,
        jsons = document.querySelectorAll(selector);

    for (i = 0; i < jsons.length; i++) {

        value = unescape(jsons[i].innerHTML);
        jsons[i].innerHTML = "";

        editor = CodeMirror(jsons[i], {
            lineNumbers: false,
            mode: mode,
            value: value,
            readOnly: true
        });
    }
}

function enableCodeMirrorMerge(selector, mode) {

    var jsons = document.querySelectorAll(selector);
    var i;

    for (i = 0; i < jsons.length; i++) {
        mergeView(jsons[i], mode);
    }

}
function mergeView(target, mode) {

    var editor,
        expectedValue, actualValue,
        expected = target.querySelector('.expected'),
        actual = target.querySelector('.actual');

    expectedValue = unescape(expected.innerHTML);
    actualValue = unescape(actual.innerHTML);

    actual.parentNode.removeChild(actual);
    expected.parentNode.removeChild(expected);
    target.innerHTML = '';

    editor = CodeMirror.MergeView(target, {
        value: expectedValue,
        origLeft: null,
        orig: actualValue,
        lineNumbers: false,
        revertButtons: false,
        mode: mode,
        highlightDifferences: true,
        collapseIdentical: false
//		connect: "align"
    });
}