window.addEventListener('load', ready, false);

function ready() {
    enableCodeMirror('.json:not(.rest-failure)', {name: 'javascript', json: true});
    enableCodeMirrorMerge('.json.rest-failure', {name: 'javascript', json: true});
    enableCodeMirror('.xml:not(.rest-failure)', 'application/xml');
    enableCodeMirrorMerge('.xml.rest-failure', 'application/xml');
    enableCodeMirror('.text:not(.rest-failure)', 'text/plain');
    enableCodeMirrorMerge('.text.rest-failure', 'text/plain');
    enableCodeMirror('.htmlmixed', 'text/html');
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
        var el = jsons[i];
        value = unescape(el.innerHTML);
        el.innerHTML = "";

        editor = CodeMirror(el, {
            lineNumbers: (el.getAttribute("lineNumbers") === 'true'),
            mode: mode,
            value: value,
            readOnly: true,
            scrollbarStyle: "simple",
            viewportMargin: Infinity
            //theme: "idea"
        });
    }
}

function enableCodeMirrorMerge(selector, mode) {
    var view, jsons = document.querySelectorAll(selector);
    for (var i = 0; i < jsons.length; i++) {
        var target = jsons[i];
        var expectedValue, actualValue,
            expected = target.querySelector('.expected'),
            actual = target.querySelector('.actual');

        expectedValue = unescape(expected.innerHTML);
        actualValue = unescape(actual.innerHTML);

        actual.parentNode.removeChild(actual);
        expected.parentNode.removeChild(expected);
        target.innerHTML = '';

        view = CodeMirror.MergeView(target, {
            value: expectedValue,
            readOnly: true,
            origLeft: null,
            orig: actualValue,
            lineNumbers: true,
            mode: mode,
            connect: true,
            highlightDifferences: true,
            collapseIdentical: true,
            scrollbarStyle: "simple",
            viewportMargin: Infinity
            //theme: "idea"
        });
    }
}