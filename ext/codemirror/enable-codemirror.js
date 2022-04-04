window.addEventListener('load', ready, false);

function ready() {
    enableCodeMirror('.json:not(.rest-failure)', {name: 'javascript', json: true});
    enableCodeMirrorMerge('.json.rest-failure', {name: 'javascript', json: true});
    enableCodeMirror('.xml:not(.rest-failure)', 'application/xml');
    enableCodeMirrorMerge('.xml.rest-failure', 'application/xml');
    enableCodeMirror('.text:not(.rest-failure)', 'text/plain');
    enableCodeMirrorMerge('.text.rest-failure', 'text/plain');
    enableCodeMirrorWithTheme('.htmlmixed.darcula', 'text/html', 'darcula');
    enableCodeMirrorWithTheme('.handlebars.darcula', {name: "handlebars", base: "text/html"}, 'darcula');
    enableCodeMirror('.htmlmixed:not(.rest-failure) .htmlmixed:not(.darcula)', 'text/html');
    enableCodeMirrorMerge('.htmlmixed.rest-failure', 'text/html');
    enableCodeMirrorHttp('.http:not(.rest-failure)');
    document.querySelectorAll('.http, .text, .json, .xml, .htmlmixed').forEach(function (el) {
        el.style.visibility = "visible";
    });
    document.querySelectorAll('.default-collapsed').forEach(function (el) {
        el.click();
    });
    window.dispatchEvent(new Event('resize'));
}

function unescape(input) {
    const e = document.createElement('div');
    e.innerHTML = input;
    return e.childNodes.length === 0 ? "" : e.childNodes[0].nodeValue;
}

function enableCodeMirrorHttp(selector) {
    let editor, jsons = document.querySelectorAll(selector);
    for (let i = 0; i < jsons.length; i++) {
        editor = CodeMirror.fromTextArea(jsons[i], {
            mode: 'message/http',
            readOnly: true,
            scrollbarStyle: "simple",
            viewportMargin: Infinity
        });
    }
}

function enableCodeMirrorWithTheme(selector, mode, theme) {
    let value, editor, jsons = document.querySelectorAll(selector);

    for (let i = 0; i < jsons.length; i++) {
        const el = jsons[i];
        value = unescape(el.innerHTML);
        el.innerHTML = "";

        editor = CodeMirror(el, {
            lineNumbers: (el.getAttribute("lineNumbers") === 'true'),
            mode: mode,
            value: value,
            readOnly: true,
            scrollbarStyle: "simple",
            viewportMargin: Infinity,
            theme: theme
        });
    }
}

function enableCodeMirror(selector, mode) {
    enableCodeMirrorWithTheme(selector, mode, "default");
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