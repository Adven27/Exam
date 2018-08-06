# DELETE-request
## `<e:delete url="..." type="<optional>">`

<div>
    <e:summary/>
    <e:given>
        Given server, that mirrors DELETE-request
    </e:given>
    <e:example name="Body check" status="ExpectedToFail" print="true">
        <e:delete url="relative/url">
            <e:case desc="Request with params (happy-path)" urlParams="param1=1&amp;param2=2">
                <expected>
                    { "delete": "/relative/url?param1=1&amp;param2=2" }
                </expected>
            </e:case>
            <e:case desc="Request without params (wrong response body)">
                <expected>
                    { "delete": "/relative/url?noparams" }
                </expected>
                <e:check>
                    <span c:assertTrue="true">Block for additional checks</span>
                </e:check>
            </e:case>
        </e:delete>
    </e:example>
    <e:example name="Status code check" status="ExpectedToFail" print="true">
        <e:delete url="status/400">
            <e:case desc="Wrong status code">
                <expected>
                    {"delete": "/status/400"}
                </expected>
            </e:case>
        </e:delete>
    </e:example>
    <e:example name="Cookies" print="true">
        <e:delete url="relative/url" cookies="cook=from_command">
            <e:case desc="Can be set in command">
                <expected>
                    {
                      "delete": "/relative/url",
                      "cookies": { "cook": "from_command"}
                    }
                </expected>
            </e:case>
            <e:case cookies="cook=from_case" desc="Can be override by case">
                <expected>
                    {
                      "delete": "/relative/url",
                      "cookies": { "cook": "from_case"}
                    }
                </expected>
            </e:case>
            <e:case cookies="cook=${#url},anotherCook=asd" desc="Placeholders can be used">
                <expected>
                    {
                      "delete": "/relative/url",
                      "cookies": {
                        "cook": "relative/url",
                        "anotherCook": "asd"
                      }
                    }
                </expected>
                <e:check>
                    Last response saved in variable <var>#exam_response</var><br/>
                    <ol>
                        <li><code c:execute="#ck = #exam_response.cookies()">c:execute="#ck = #exam_response.cookies()</code></li>
                        <li>echo #exam_response.cookies() => <code c:echo="#exam_response.cookies()"/></li>
                        <li>echo #ck => <code c:echo="#ck"/></li>
                    </ol>
                </e:check>
            </e:case>
            <e:case cookies="${#exam_response.cookies()}" desc="If @FullOGNL is enabled, response fields can be accessed, e.g. ${#exam_response.cookies()}">
                <expected>
                    {
                      "delete": "/relative/url",
                      "cookies": {
                        "cook": "relative/url",
                        "anotherCook": "asd"
                      }
                    }
                </expected>
            </e:case>
        </e:delete>
    </e:example>
</div>