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
        </e:given>
        <e:when>When</e:when>
        <e:then>
            <var>#myOuterVar</var><code c:echo="#myOuterVar"/>
            <var>#myInnerVar</var><code c:echo="#myInnerVar"/>
        </e:then>
    </e:example>
    <e:example name="Some example 2"/>
    <var>#myOuterVar</var><code c:echo="#myOuterVar"/>
    <var>#myInnerVar</var><code c:echo="#myInnerVar"/>
</div>