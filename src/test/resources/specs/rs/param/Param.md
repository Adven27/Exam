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
                <where vars="p1, p2" separator="|">
                    <vals>first value of p1| first value of p2</vals>
                    <vals>second value of p1| second value of p2</vals>
                    <vals>${exam.now-[2 months, 1 y]:dd.MM.yyyy}| ${exam.now-[2 months, 2 y]:dd.MM.yyyy}</vals>
                </where>
            </e:case>
        </e:post>
    </e:example>
</div>