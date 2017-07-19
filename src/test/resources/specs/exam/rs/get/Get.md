# GET-запрос: `<e:rs-get url="..." type="<optional>">`

Допустим имеется сервер, который в ответ на `GET` присылает в теле ответа строку запроса

### [**Пример проверок тела ответа**](- "response body check c:status=ExpectedToFail")

    <e:rs-get url="relative/url">
        <e:rs-case desc="Запрос с параметрами (все проверки успешны)" urlParams="param1=1&amp;param2=2">        
            <expected>
                { "get": "/relative/url?param1=1&amp;param2=2" }
            </expected>
        </e:rs-case>
        <e:rs-case desc="Запрос без параметров (с неверным телом ответа)">        
            <expected>
                { "get": "/relative/url?noparams" }
            </expected>
            <e:check>
                <span c:assertTrue="true">Произвольный блок, где можно сделать дополнительные проверки, относящиеся к данному кейсу</span>
            </e:check>
        </e:rs-case>
    </e:rs-get>

<div>
    <e:rs-get url="relative/url">
        <e:rs-case desc="Запрос с параметрами (все проверки успешны)" urlParams="param1=1&amp;param2=2">        
            <expected>
                { "get": "/relative/url?param1=1&amp;param2=2" }
            </expected>
        </e:rs-case>
        <e:rs-case desc="Запрос без параметров (с неверным телом ответа)">        
            <expected>
                { "get": "/relative/url?noparams" }
            </expected>
            <e:check>
                <span c:assertTrue="true">Произвольный блок, где можно сделать дополнительные проверки, относящиеся к данному кейсу</span>
            </e:check>
        </e:rs-case>
    </e:rs-get>
</div>

### [**Пример проверки кода ответа**](- "wrong status code c:status=ExpectedToFail")

    <e:rs-get url="status/400">
        <e:rs-case desc="Неверный код ответа">        
            <expected>
                {"get": "/status/400"}
            </expected>
        </e:rs-case>
    </e:rs-get>   

<div>
    <e:rs-get url="status/400">
        <e:rs-case desc="Неверный код ответа">        
            <expected>
                {"get": "/status/400"}
            </expected>
        </e:rs-case>
    </e:rs-get>   
</div>

### [**Пример работы с куками**](- "cookies")

    <e:rs-get url="relative/url" cookies="cook=from_command">
        <e:rs-case desc="Если у кейса не указаны - используются заданные в команде">        
            <expected>
                {
                  "get": "/relative/url",
                  "cookies": { "cook": "from_command"}
                }
            </expected>
        </e:rs-case>
        <e:rs-case cookies="cook=from_case" desc="Иначе, те что у кейса">        
            <expected>
                {
                  "get": "/relative/url",
                  "cookies": { "cook": "from_case"}
                }
            </expected>
        </e:rs-case>
        <e:rs-case cookies="cook=${var.url}" desc="Можно использовать плейсхолдеры для вызова переменных и методов">        
            <expected>
                {
                  "get": "/relative/url",
                  "cookies": {"cook": "relative/url" }
                }
            </expected>
        </e:rs-case>
    </e:rs-get>

<div>
    <e:rs-get url="relative/url" cookies="cook=from_command">
        <e:rs-case desc="Если у кейса не указаны - используются заданные в команде">        
            <expected>
                {
                  "get": "/relative/url",
                  "cookies": { "cook": "from_command"}
                }
            </expected>
        </e:rs-case>
        <e:rs-case cookies="cook=from_case" desc="Иначе, те что у кейса">        
            <expected>
                {
                  "get": "/relative/url",
                  "cookies": { "cook": "from_case"}
                }
            </expected>
        </e:rs-case>
        <e:rs-case cookies="cook=${var.url}" desc="Можно использовать плейсхолдеры для вызова переменных и методов">        
            <expected>
                {
                  "get": "/relative/url",
                  "cookies": {"cook": "relative/url" }
                }
            </expected>
        </e:rs-case>
    </e:rs-get>
</div>