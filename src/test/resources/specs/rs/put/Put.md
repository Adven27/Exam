# PUT-request
## `<e:put url="..." type="<optional>">`

<div>
    <e:summary/>
    <e:given>
        Given server, that mirrors request
    </e:given>
    <e:example name="Body check" status="ExpectedToFail" print="true">
         <e:put url="relative/url">
            <e:case desc="Happy-path">
                <body>
                    {"exact": "ok", "template": 1}
                </body>
                <expected>
                    {"exact": "ok", "template": "!{number}"}
                </expected>
                <e:check>
                  <span c:assertTrue="true">Block for additional checks</span>
                </e:check>
            </e:case>
            <e:case desc="Неверный ответ">
                <body>
                    {"exact": "not ok", "template": "not number"}
                </body>
                <expected>
                    {"exact": "ok", "template": "!{number}"}
                </expected>
            </e:case>
        </e:put>
    </e:example>
    <e:example name="Status code check" status="ExpectedToFail" print="true">
        <e:put url="status/400" type="text/plain">
            <e:case desc="Placeholders can be used inside body block">
                <body>
                    {"url": "${#url}", "template": 1}
                </body>
                <expected>
                    {"url": "status/400", "template": "!{number}"}
                </expected>
            </e:case>
        </e:put>
    </e:example>
    <e:example name="Cookies" print="true">
        <e:put url="cookies" cookies="cook=from_command">
            <e:case desc="Can be set in command">
                <body/>
                <expected>
                    {
                      "cookies": { "cook": "from_command"}
                    }
                </expected>
            </e:case>
            <e:case cookies="cook=from_case" desc="Can be override by case">
                <body/>
                <expected>
                    {
                      "cookies": { "cook": "from_case"}
                    }
                </expected>
            </e:case>
            <e:case cookies="cook=${#url}" desc="Placeholders can be used">
                <body/>
                <expected>
                    {
                      "cookies": {"cook": "cookies" }
                    }
                </expected>
            </e:case>
        </e:put>
    </e:example>
</div>