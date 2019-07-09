# Handlebar support: {{...}}

<div>
    <e:summary/>
    <e:example name="Access to concordion variables">
        <e:given>
            <code c:set="#someVar">someValue</code>
        </e:given>
        <e:post url="some/url" type="text/plain" print="true">
            <e:case desc="You can have access to concordion variables with {{someVar}}">
                <body>
                    {"var": "{{someVar}}"}
                </body>
                <expected>
                    {"var": "someValue"}
                </expected>
            </e:case>
        </e:post>
    </e:example>
    <e:example name="DateTime support">
        <e:post url="some/url" type="text/plain" print="true">
            <e:case desc="There are constants of Date type for yesterday/now/tomorrow">
                <body>
                    {
                     "yesterday": "{{now minus='1 d'}}",
                     "now":       "{{now}}",
                     "tomorrow":  "{{now plus='1 d'}}"
                    }
                </body>
                <expected>
                    {
                     "yesterday": "{{string}}",
                     "now":       "{{string}}",
                     "tomorrow":  "{{string}}"
                    }
                </expected>
            </e:case>
            <e:case desc="You can format the output of this constants">
                <body>
                    {
                     "yesterday": "{{now "dd.MM.yyyy'T'hh:mm:ss" minus="1 d"}}",
                     "now":       "{{now "dd.MM.yyyy"}}",
                     "tomorrow":  "{{now "yyyy-MM-dd" plus="1 d"}}"
                    }
                </body>
                <expected>
                    {
                     "yesterday": "{{formattedAs "dd.MM.yyyy'T'hh:mm:ss"}}",
                     "now":       "{{formattedAs 'dd.MM.yyyy'}}",
                     "tomorrow":  "{{formattedAs 'yyyy-MM-dd'}}"
                    }
                </expected>
            </e:case>
            <e:case desc="You can get any time from now with +/- period">
                <body>
                    {
                     "endFromNow": "{{now 'dd.MM.yyyy' plus='day 1, 2 months, 3 y'}}",
                     "startFromNow": "{{now 'dd.MM.yyyy' minus='day 1, 2 months, 3 y'}}"
                    }
                </body>
                <expected>
                    {
                     "endFromNow": "{{formattedAs 'dd.MM.yyyy'}}",
                     "startFromNow": "{{formattedAs 'dd.MM.yyyy'}}"
                    }
                </expected>
            </e:case>
            <e:case desc="You can set arbitrary date">
                <body>
                    {
                     "date": "{{date '1951-05-14'}}",
                     "formattedDate": "{{dateFormat (date '14.05.1951' format='dd.MM.yyyy') 'dd.MM.yyyy'}}"
                    }
                </body>
                <expected>
                    {
                     "date": "{{string}}",
                     "formattedDate": "{{formattedAndWithin 'dd.MM.yyyy' '1d' '13.05.1951'}}"
                    }
                </expected>
            </e:case>
        </e:post>
    </e:example>
</div>