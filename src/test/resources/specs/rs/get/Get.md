# GET-request
## `<e:get url="..." type="<optional>">`

<div>
    <e:summary/>
    <e:given>
        Given server, that mirrors GET-request
    </e:given>
    <e:example name="Body check" status="ExpectedToFail" print="true">
        <e:get url="relative/url">
            <e:case desc="Request with params (happy-path)" urlParams="param1=1&amp;param2=2">
                <expected>
                    { "GET": "/relative/url?param1=1&amp;param2=2" }
                </expected>
            </e:case>
            <e:case desc="Request without params (wrong response body)">
                <expected>
                    { "GET": "/relative/url?noparams" }
                </expected>
                <e:check>
                    <span c:assertTrue="true">Block for additional checks</span>
                </e:check>
            </e:case>
        </e:get>
    </e:example>
    <e:example name="Status code check" status="ExpectedToFail" print="true">
        <e:get url="status/400">
            <e:case desc="Wrong status code">
                <expected>
                    {"GET": "/status/400"}
                </expected>
            </e:case>
        </e:get>
    </e:example>
    <e:example name="Check failed status code" print="true">
        <e:get url="status/400">
            <e:case desc="Wrong status code was expected">
                <expected statusCode="400" reasonPhrase="Bad Request">
                    {"GET": "/status/400"}
                </expected>
            </e:case>
        </e:get>
    </e:example>
    <e:example name="Cookies" print="true">
        <e:get url="relative/url" cookies="cook=from_command">
            <e:case desc="Can be set in command">
                <expected>
                    {
                      "GET": "/relative/url",
                      "cookies": "{cook=from_command}"
                    }
                </expected>
            </e:case>
            <e:case cookies="cook=from_case" desc="Can be override by case">
                <expected>
                    {
                      "GET": "/relative/url",
                      "cookies": "{cook=from_case}"
                    }
                </expected>
            </e:case>
            <e:case cookies="cook={{url}},anotherCook=asd" desc="Placeholders can be used">
                <expected>
                    {
                      "GET": "/relative/url",
                      "cookies": "{cook=relative/url, anotherCook=asd}"
                    }
                </expected>
                <e:check>
                    Last response saved in variable <var>#exam_response</var><br/>
                    <ol>
                        <li><code c:execute="#my_header = #exam_response.header('my_header')">c:execute="#my_header = #exam_response.header('my_header')</code></li>
                        <li>echo #exam_response.header('my_header') => <code c:echo="#exam_response.header('my_header')"/></li>
                        <li>echo #my_header => <code c:echo="#my_header"/></li>
                    </ol>
                </e:check>
            </e:case>
            <e:case cookies="cook={{eval &quot;#exam_response.header('my_header')&quot;}}" desc="If @FullOGNL is enabled, response fields can be accessed">
                <expected>
                    {
                      "GET": "/relative/url",
                      "cookies": "{cook=some value}"
                    }
                </expected>
            </e:case>
        </e:get>
    </e:example>
    <e:example name="Ignore json paths" print="true">
        <span>ignored JSON paths should be placed in "expected" at "ignoredPaths" attribute separated by ";". </span>
        <e:get url="ignoreJson">
            <e:case desc="JSON nodes can be ignored">
                <expected ignoredPaths="param2;arr[*].param4">
                    {
                      "param1":"value1",
                      "arr":[
                      {"param3":"value3"},
                      {"param3":"value3"}
                      ]
                    }
                </expected>
            </e:case>
        </e:get>
        <e:get url="ignoreJsonArray">
            <e:case desc="JSON nodes in array can be ignored">
                <expected ignoredPaths="[*].param4">
                    [
                      {"param3":"value3"},
                      {"param3":"value3"}
                    ]
                </expected>
            </e:case>
         </e:get>
    </e:example>  
    <e:example name="Configure json compare mode" print="true">
        <span>Specific configuration for json-unit should be placed in "expected" at "jsonUnitOptions" attribute separated by ";". </span>
        <e:get url="ignoreJson">
            <e:case desc="Extra fields can be ignored">
                <expected jsonUnitOptions="IGNORING_EXTRA_FIELDS">
                    {
                      "param1":"value1",
                      "arr":[
                      {"param3":"value3"},
                      {"param3":"value3"}
                      ]
                    }
                </expected>
            </e:case>
        </e:get>
    </e:example>
</div>