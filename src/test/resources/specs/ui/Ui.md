# UI testing

<div>
    <e:example name="UI" print="true">
        <e:browser url=":8081/ui">
            <step name="hasText">Dummy page</step>
            <step name="hasText">Dummy page</step>
            <step set="someVar" name="noParamsCheck"/>
        </e:browser>
        <var>#someVar</var> = <code c:assertTrue="areEqual(#someVar, #TEXT)">valueFromMethodCall</code>
    </e:example>
</div>