# UI testing

<div>
    <e:example name="UI">
        <e:when>
            <e:browser url=":8081/ui" log="true">
                <step name="hasText">Dummy page</step>
                <step name="noParamsCheck"/>
            </e:browser>
        </e:when>
    </e:example>
</div>