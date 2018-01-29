# UI testing

<div>
    <e:summary/>
    <e:example name="Steps chain fail fast by default" print="true" status="ExpectedToFail">
        <e:browser url=":8081/ui">
            <step name="hasText">Should fail</step>
            <step name="hasText">Shouldn't be ran</step>
        </e:browser>
    </e:example>
    <e:example name="Can disable fail fast" print="true" status="ExpectedToFail">
        <e:browser url=":8081/ui" failFast="false">
            <step name="hasText">Fails but next step should run</step>
            <step name="hasText" desc="Should successfully check text">Dummy page</step>
        </e:browser>
    </e:example>
    <e:example name="Can set check result to variable" print="true">
        <e:browser url=":8081/ui">
            <step set="someVar" name="noParamsCheck"/>
        </e:browser>
        <var>#someVar</var> = <code c:assertTrue="areEqual(#someVar, #TEXT)">valueFromMethodCall</code>
    </e:example>
</div>