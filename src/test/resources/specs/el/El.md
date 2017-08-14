# Exam expression language: ${...}

<div>
    <e:example name="Access to concordion variables">
        <e:given>
            <code c:set="#someVar">someValue</code>
        </e:given>
        <e:rs-post url="some/url" type="text/plain">
            <e:rs-case desc="You can have access to concordion variables with ">
                <body>
                    {"var.someVar": "${var.someVar}"}
                </body>
                <expected>
                    {"var.someVar": "someValue"}
                </expected>
            </e:rs-case>
        </e:rs-post>
    </e:example>
    <e:example name="DateTime support">
        <e:rs-post url="some/url" type="text/plain">
            <e:rs-case desc="There are constants of Date type for yesterday/now/tomorrow">
                <body>
                    {
                     "exam.yesterday": "${exam.yesterday}",
                     "exam.now":       "${exam.now}",
                     "exam.tomorrow":  "${exam.tomorrow}"
                    }
                </body>
                <expected>
                    {
                     "exam.yesterday": "${json-unit.any-string}",
                     "exam.now":       "${json-unit.any-string}",
                     "exam.tomorrow":  "${json-unit.any-string}"
                    }
                </expected>
            </e:rs-case>
            <e:rs-case desc="You can format the output of this constants">
                <body>
                    {
                     "exam.yesterday:dd.MM.yyyy'T'hh:mm:ss": "${exam.yesterday:dd.MM.yyyy'T'hh:mm:ss}",
                     "exam.now:dd.MM.yyyy":       "${exam.now:dd.MM.yyyy}",
                     "exam.tomorrow:yyyy-MM-dd":  "${exam.tomorrow:yyyy-MM-dd}"
                    }
                </body>
                <expected>
                    {
                     "exam.yesterday:dd.MM.yyyy'T'hh:mm:ss": "${json-unit.matches:dd.MM.yyyy'T'hh:mm:ss}",
                     "exam.now:dd.MM.yyyy":       "${json-unit.matches:dd.MM.yyyy}",
                     "exam.tomorrow:yyyy-MM-dd":  "${json-unit.matches:yyyy-MM-dd}"
                    }
                </expected>
            </e:rs-case>
            <e:rs-case desc="You can get any time from now with +/- period">
                <body>
                    {
                     "exam.now+[day 1, 2 months, 3 y]": "${exam.now+[day 1, 2 months, 3 y]:dd.MM.yyyy}",
                     "exam.now-[day 1, 2 months, 3 y]": "${exam.now-[day 1, 2 months, 3 y]:dd.MM.yyyy}"
                    }
                </body>
                <expected>
                    {
                     "exam.now+[day 1, 2 months, 3 y]": "${json-unit.matches:dd.MM.yyyy}",
                     "exam.now-[day 1, 2 months, 3 y]": "${json-unit.matches:dd.MM.yyyy}"
                    }
                </expected>
            </e:rs-case>
            <e:rs-case desc="You can set arbitrary date with ">
                <body>
                    {
                     "exam.date(dd.MM.YYY)": "${exam.date(14.05.1951)}",
                     "exam.date(dd.MM.YYY):yyyy-MM-dd": "${exam.date(14.05.1951):yyyy-MM-dd}"
                    }
                </body>
                <expected>
                    {
                     "exam.date(dd.MM.YYY)": "${json-unit.any-string}",
                     "exam.date(dd.MM.YYY):yyyy-MM-dd": "${json-unit.matches:yyyy-MM-dd}"
                    }
                </expected>
            </e:rs-case>
        </e:rs-post>
    </e:example>
</div>