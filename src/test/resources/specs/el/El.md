# Exam expression language: ${...}

<div>
    <e:example name="Access to concordion variables">
        <e:given>
            <code c:set="#someVar">someValue</code>
        </e:given>
        <e:rs-post url="some/url" type="text/plain" print="true">
            <e:rs-case desc="You can have access to concordion variables with ">
                <body>
                    {"var": "${var.someVar}"}
                </body>
                <expected>
                    {"var": "someValue"}
                </expected>
            </e:rs-case>
        </e:rs-post>
    </e:example>
    <e:example name="DateTime support">
        <e:rs-post url="some/url" type="text/plain" print="true">
            <e:rs-case desc="There are constants of Date type for yesterday/now/tomorrow">
                <body>
                    {
                     "yesterday": "${exam.yesterday}",
                     "now":       "${exam.now}",
                     "tomorrow":  "${exam.tomorrow}"
                    }
                </body>
                <expected>
                    {
                     "yesterday": "!{str}",
                     "now":       "!{str}",
                     "tomorrow":  "!{str}"
                    }
                </expected>
            </e:rs-case>
            <e:rs-case desc="You can format the output of this constants">
                <body>
                    {
                     "yesterday": "${exam.yesterday:dd.MM.yyyy'T'hh:mm:ss}",
                     "now":       "${exam.now:dd.MM.yyyy}",
                     "tomorrow":  "${exam.tomorrow:yyyy-MM-dd}"
                    }
                </body>
                <expected>
                    {
                     "yesterday": "!{formattedAs dd.MM.yyyy'T'hh:mm:ss}",
                     "now":       "!{formattedAs dd.MM.yyyy}",
                     "tomorrow":  "!{formattedAs yyyy-MM-dd}"
                    }
                </expected>
            </e:rs-case>
            <e:rs-case desc="You can get any time from now with +/- period">
                <body>
                    {
                     "endFromNow": "${exam.now+[day 1, 2 months, 3 y]:dd.MM.yyyy}",
                     "startFromNow": "${exam.now-[day 1, 2 months, 3 y]:dd.MM.yyyy}"
                    }
                </body>
                <expected>
                    {
                     "endFromNow": "!{formattedAs dd.MM.yyyy}",
                     "startFromNow": "!{formattedAs dd.MM.yyyy}"
                    }
                </expected>
            </e:rs-case>
            <e:rs-case desc="You can set arbitrary date">
                <body>
                    {
                     "date": "${exam.date(14.05.1951)}",
                     "formattedDate": "${exam.date(14.05.1951):yyyy-MM-dd}"
                    }
                </body>
                <expected>
                    {
                     "date": "!{any-string}",
                     "formattedDate": "!{formattedAndWithin [yyyy-MM-dd][1d][1951-05-13]}"
                    }
                </expected>
            </e:rs-case>
        </e:rs-post>
    </e:example>
</div>