# Check directory content
## `<e:fl-check dir="dir"/>`

***!!! Only XML comparing are supported !!!***

<div>
    <e:summary/>
    <e:given>
        <e:fl-set dir="dir">
            <e:file name="not_empty_file" from="/data/actual.xml"/>
            <e:file name="empty_file"/>
        </e:fl-set>
    </e:given>
    <e:example name="Happy-path">
        <e:then print="true">
            <e:fl-check dir="dir">
                <e:file name="empty_file"/>
                <e:file name="not_empty_file" from="/data/actual.xml"/>
            </e:fl-check>
        </e:then>
    </e:example>
     <e:example name="Happy-path with parametrized file name">
        <e:then print="true">
            <e:fl-check dir="dir">
                <e:file name="{{emptyFileNameFromTemplate}}"/>
                <e:file name="not_empty_file" from="/data/actual.xml"/>
            </e:fl-check>
        </e:then>
    </e:example>
    <e:example name="Surplus file" status="ExpectedToFail">
        <e:then print="true">
            <e:fl-check dir="dir">
                <e:file name="not_empty_file"/>
            </e:fl-check>
        </e:then>
    </e:example>
    <e:example name="Missing file" status="ExpectedToFail">
        <e:then print="true">
            <e:fl-check dir="dir">
                <e:file name="empty_file"/>
                <e:file name="missing_file"/>
                <e:file name="not_empty_file"/>
            </e:fl-check>
        </e:then>
    </e:example>
    <e:example name="Wrong file content" status="ExpectedToFail">
        <e:then print="true">
            <e:fl-check dir="dir">
                <e:file name="empty_file"/>
                <e:file name="not_empty_file">another content was expected</e:file>
            </e:fl-check>
        </e:then>
    </e:example>
    <e:example name="All previous checks together" status="ExpectedToFail">
        <e:then print="true">
            <e:fl-check dir="dir">
                <e:file name="missing_file"/>
                <e:file name="not_empty_file">another content was expected</e:file>
            </e:fl-check>
        </e:then>
    </e:example>
    <e:example name="Details">
        <ol>
            <li><a c:run="concordion" href="details/Details.html">File comparing details</a></li>
        </ol>
    </e:example>
</div>