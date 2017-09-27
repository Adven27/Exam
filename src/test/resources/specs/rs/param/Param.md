# POST-request
## `<e:post url="..." type="<optional>">`

<div>
    <e:summary/>
    <e:given>
        Given server, that mirrors request
    </e:given>
    <e:example name="Parametrized cases" status="ExpectedToFail">
        <e:post url="relative/url" print="true">
            <e:case desc="Wrong response" variables="p1:p2" values="value of p1:value of p2,second variant for p1: second variant for p2">
                <body>
                    {"exact": "${#p1}", "template": 1}
                </body>
                <expected>
                    {"exact": "${#p2}", "template": "!{number}"}
                </expected>
            </e:case>
        </e:post>
    </e:example>
</div>