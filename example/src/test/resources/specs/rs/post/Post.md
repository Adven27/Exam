# POST-request
## `<e:post url="..." type="<optional>">`

<div>
    <e:summary/>
    <e:given>
        Given server, that mirrors request
    </e:given>
    <e:example name="Body check" status="ExpectedToFail" print="true">
         <e:post url="relative/url">
            <e:case desc="Happy-path">
                <body>
                    {"exact": "ok", "template": 1}
                </body>
                <expected>
                    {"exact": "ok", "template": "{{number}}"}
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
                    {"exact": "ok", "template": "{{number}}"}
                </expected>
            </e:case>
        </e:post>
    </e:example>
    <e:example name="Status code check" status="ExpectedToFail" print="true">
        <e:post url="status/400" type="text/plain">
            <e:case desc="Placeholders can be used inside body block">
                <body>
                    {"url": "{{url}}", "template": 1}
                </body>
                <expected>
                    {"url": "status/400", "template": "{{number}}"}
                </expected>
            </e:case>
        </e:post>
    </e:example>
    <e:example name="Cookies" print="true">
        <e:post url="cookies" cookies="cook=from_command">
            <e:case desc="Can be set in command">
                <body/>
                <expected>
                    {
                      "cookies": "{cook=from_command}"
                    }
                </expected>
            </e:case>
            <e:case cookies="cook=from_case" desc="Can be override by case">
                <body/>
                <expected>
                    {
                      "cookies": "{cook=from_case}"
                    }
                </expected>
            </e:case>
            <e:case cookies="cook={{url}}" desc="Placeholders can be used">
                <body/>
                <expected>
                    {
                      "cookies": "{cook=cookies}"
                    }
                </expected>
            </e:case>
        </e:post>
    </e:example>
    <e:example name="Long Cookies display" print="true">
        <e:post url="cookies" cookies="cook=long_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_value">
            <e:case desc="Can be set in command">
                <body/>
                <expected>
                    {
                      "cookies": "{cook=long_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_valuelong_long_value}"
                    }
                </expected>
            </e:case>
        </e:post>
    </e:example>
    <e:example name="POST with url params" print="true">
        <e:post url="/method/withParams">
            <e:case desc="POST can send parameters in url" urlParams="param1=1&amp;param2=2">
                <body>
                    {
                      "bodyValue": 111
                    }
                </body>
                <expected>
                    {
                      "request": {
                        "POST": "/method/withParams?param1=1&amp;param2=2"
                      },
                      "body": {
                        "bodyValue": 111
                      }
                    }
                </expected>
            </e:case>
        </e:post>
    </e:example>
</div>