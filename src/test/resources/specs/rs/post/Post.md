# POST-запрос: `<e:rs-post url="..." type="<optional>">`

Допустим имеется сервер, который в ответ на `POST` присылает в теле ответа ровно то же, что было в теле запроса

<div>
<e:summary/>
<e:example name="Пример проверок тела ответа" status="ExpectedToFail" print="true">
     <e:post url="relative/url">
        <e:case desc="Happy-path">        
            <body>
                {"exact": "ok", "template": 1}
            </body>
            <expected>
                {"exact": "ok", "template": "!{number}"}
            </expected>
            <e:check>
              <span c:assertTrue="true">Произвольный блок, где можно сделать дополнительные проверки, относящиеся к данному кейсу</span>
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
    </e:post>
</e:example> 
<e:example name="Пример проверки кода ответа" status="ExpectedToFail" print="true">
    <e:post url="status/400" type="text/plain">
        <e:case desc="В блоке body можно использовать плейсхолдеры для вызова переменных и методов">        
            <body>
                {"url": "${var.url}", "template": 1}
            </body>
            <expected>
                {"url": "status/400", "template": "!{number}"}
            </expected>
        </e:case>
    </e:post>
</e:example>
<e:example name="Пример работы с куками" print="true">
    <e:post url="cookies" cookies="cook=from_command">
        <e:case desc="Если у кейса не указаны - используются заданные в команде">        
            <body/>
            <expected>
                {                 
                  "cookies": { "cook": "from_command"}
                }
            </expected>
        </e:case>
        <e:case cookies="cook=from_case" desc="Иначе, те что у кейса">        
            <body/>
            <expected>
                {
                  "cookies": { "cook": "from_case"}
                }
            </expected>
        </e:case>
        <e:case cookies="cook=${var.url}" desc="Можно использовать плейсхолдеры для вызова переменных и методов">        
            <body/>
            <expected>
                {
                  "cookies": {"cook": "cookies" }
                }
            </expected>
        </e:case>
    </e:post>
</e:example>
</div>