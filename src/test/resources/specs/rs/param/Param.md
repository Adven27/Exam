# POST-request
## `<e:post url="..." type="<optional>">`

<div>
    <e:summary/>
    <e:given>
        Given server, that mirrors request
    </e:given>
    <e:example name="Parametrized cases" status="ExpectedToFail">
        <e:post url="relative/url" print="true">
            <e:case desc="Wrong response"  values=",">
                <body>
                    {"exact": "${#p1}", "template": 1}
                </body>
                <expected>
                    {"exact": "${#p2}", "template": "!{number}"}
                </expected>
                <where vars="p1, p2">
                    <vals>first value of p1, first value of p2</vals>
                    <vals>second value of p1, second value of p2</vals>
                </where>
            </e:case>
        </e:post>
    </e:example>
</div>