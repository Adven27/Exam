# UI testing

<div>
    <e:example name="UI">
        <e:browser url=":8081/ui" log="true">
            <step name="hasText">Dummy page</step>
            <step set="someVar" name="noParamsCheck"/>
        </e:browser>
        #someVar = <code c:assertTrue="areEqual(#someVar, #TEXT)">valueFromMethodCall</code>
    </e:example>
</div>