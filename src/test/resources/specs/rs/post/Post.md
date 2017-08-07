# POST-запрос: `<e:rs-post url="..." type="<optional>">`

Допустим имеется сервер, который в ответ на `POST` присылает в теле ответа ровно то же, что было в теле запроса

### [**Пример проверок тела ответа**](- "response body check c:status=ExpectedToFail")

    <e:rs-post url="relative/url">
        <e:rs-case desc="Happy-path">        
            <body>
                {"exact": "ok", "template": 1}
            </body>
            <expected>
                {"exact": "ok", "template": "${json-unit.any-number}"}
            </expected>
            <e:check>
              <span c:assertTrue="true">Произвольный блок, где можно сделать дополнительные проверки, относящиеся к данному кейсу</span>
            </e:check>
        </e:rs-case>      
        <e:rs-case desc="Неверный ответ">
            <body>
                {"exact": "not ok", "template": "not number"}
            </body>
            <expected>
                {"exact": "ok", "template": "${json-unit.any-number}"}
            </expected>
        </e:rs-case>
    </e:rs-post>  

<div>
    <e:rs-post url="relative/url">
        <e:rs-case desc="Happy-path">        
            <body>
                {"exact": "ok", "template": 1}
            </body>
            <expected>
                {"exact": "ok", "template": "${json-unit.any-number}"}
            </expected>
            <e:check>
              <span c:assertTrue="true">Произвольный блок, где можно сделать дополнительные проверки, относящиеся к данному кейсу</span>
            </e:check>
        </e:rs-case>      
        <e:rs-case desc="Неверный ответ">
            <body>
                {"exact": "not ok", "template": "not number"}
            </body>
            <expected>
                {"exact": "ok", "template": "${json-unit.any-number}"}
            </expected>
        </e:rs-case>
    </e:rs-post>    
</div>

### [**Пример проверки кода ответа**](- "wrong status code c:status=ExpectedToFail")

    <e:rs-post url="status/400" type="text/plain">
        <e:rs-case desc="В блоке body можно использовать плейсхолдеры для вызова переменных и методов">        
            <body>
                {"url": "${var.url}", "template": 1}
            </body>
            <expected>
                {"url": "status/400", "template": "${json-unit.any-number}"}
            </expected>
        </e:rs-case>
    </e:rs-post>
    
<div>
    <e:rs-post url="status/400" type="text/plain">
        <e:rs-case desc="В блоке body можно использовать плейсхолдеры для вызова переменных и методов">        
            <body>
                {"url": "${var.url}", "template": 1}
            </body>
            <expected>
                {"url": "status/400", "template": "${json-unit.any-number}"}
            </expected>
        </e:rs-case>
    </e:rs-post>
</div>

### [**Пример работы с куками**](- "cookies")

    <e:rs-post url="cookies" cookies="cook=from_command">
        <e:rs-case desc="Если у кейса не указаны - используются заданные в команде">        
            <body/>
            <expected>
                {                 
                  "cookies": { "cook": "from_command"}
                }
            </expected>
        </e:rs-case>
        <e:rs-case cookies="cook=from_case" desc="Иначе, те что у кейса">        
            <body/>
            <expected>
                {
                  "cookies": { "cook": "from_case"}
                }
            </expected>
        </e:rs-case>
        <e:rs-case cookies="cook=${var.url}" desc="Можно использовать плейсхолдеры для вызова переменных и методов">        
            <body/>
            <expected>
                {
                  "cookies": {"cook": "cookies" }
                }
            </expected>
        </e:rs-case>
    </e:rs-post>

<div>
    <e:rs-post url="cookies" cookies="cook=from_command">
        <e:rs-case desc="Если у кейса не указаны - используются заданные в команде">        
            <body/>
            <expected>
                {                 
                  "cookies": { "cook": "from_command"}
                }
            </expected>
        </e:rs-case>
        <e:rs-case cookies="cook=from_case" desc="Иначе, те что у кейса">        
            <body/>
            <expected>
                {
                  "cookies": { "cook": "from_case"}
                }
            </expected>
        </e:rs-case>
        <e:rs-case cookies="cook=${var.url}" desc="Можно использовать плейсхолдеры для вызова переменных и методов">        
            <body/>
            <expected>
                {
                  "cookies": {"cook": "cookies" }
                }
            </expected>
        </e:rs-case>
    </e:rs-post>
</div>