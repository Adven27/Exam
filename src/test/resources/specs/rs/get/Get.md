# GET-запрос: `<e:rs-get url="..." type="<optional>">`

Допустим имеется сервер, который в ответ на `GET` присылает в теле ответа строку запроса

<div>
<e:example name="Пример проверок тела ответа" status="ExpectedToFail">
    <e:rs-get url="relative/url" log="true">
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
</e:example>
<e:example name="Пример проверки кода ответа" status="ExpectedToFail">
    <e:rs-get url="status/400" log="true">
        <e:rs-case desc="Неверный код ответа">        
            <expected>
                {"get": "/status/400"}
            </expected>
        </e:rs-case>
    </e:rs-get>   
</e:example>
<e:example name="Пример работы с куками">
    <e:rs-get url="relative/url" cookies="cook=from_command" log="true">
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
        <e:rs-case cookies="cook=${var.url},anotherCook=asd" desc="Можно использовать плейсхолдеры для вызова переменных и методов">
            <expected>
                {
                  "get": "/relative/url",
                  "cookies": {
                    "cook": "relative/url",
                    "anotherCook": "asd"
                  }
                }
            </expected>
            <e:check>
                Последний респонс лежит в #exam_response<br/>
                <ol>
                <li><code c:execute="#ck = #exam_response.cookies()">c:execute="#ck = #exam_response.cookies()</code></li>
                <li>echo #exam_response.cookies() => <code c:echo="#exam_response.cookies()"/></li>
                <li>echo #ck => <code c:echo="#ck"/></li>
                </ol>
            </e:check>
        </e:rs-case>
        <e:rs-case cookies="${var.exam_response.cookies()}" desc="Можно использовать поля предыдущего респонса, например ${var.exam_response.cookies()}, если пометить спеку @FullOGNL">
            <expected>
                {
                  "get": "/relative/url",
                  "cookies": {
                    "cook": "relative/url",
                    "anotherCook": "asd"
                  }
                }
            </expected>
        </e:rs-case>
    </e:rs-get>
</e:example>
</div>