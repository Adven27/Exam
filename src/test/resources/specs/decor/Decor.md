# Decor commands

<div print="true">
    <e:summary title="Custom title"/>
    <e:given>
        <e:set var="myOuterVar" value="{{now}}"/>
    </e:given>
    <e:when>When</e:when>
    <e:then>Then</e:then>
    <e:example name="Some example 1">
        <e:given>
            <e:set var="myInnerVar" value="{{now}}"/>
            <e:set var="fromFile" from="/data/decor/some-file.txt" vars="v1=v1, v2={{date '2000-01-01'}}"/>
        </e:given>
        <e:when>When</e:when>
        <e:then>
            <var>#myOuterVar</var><code c:echo="#myOuterVar"/>
            <var>#myInnerVar</var><code c:echo="#myInnerVar"/>
            <var>#fromFile</var><code c:echo="#fromFile"/>
            Var <var>#v1</var> was removed <code c:echo="#v1"/>
        </e:then>
    </e:example>
    <e:example name="Some example 2"/>
    <var>#myOuterVar</var><code c:echo="#myOuterVar"/>
    <var>#myInnerVar</var><code c:echo="#myInnerVar"/>
    <e:include from="/keywords/some-keyword.xml"/>
</div>