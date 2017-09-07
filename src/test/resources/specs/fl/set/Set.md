# Setting directory content
## `<e:fl-set dir="dir"/>`

<div>
    <e:summary/>
    <e:given>
        Given dir <code c:echo="dir"/>
    </e:given>
    <e:example name="Directory will be cleared">
        <e:given>
            Given file <code c:set="#name">some_file</code> <span c:assertTrue="addFile(#name)">present in dir.</span>
            <e:fl-show dir="dir"/>
        </e:given>
        <e:then print="true">
            <e:fl-set dir="dir">
                <file name="empty_file"/>
                <file name="content_from_external_file" from="data/test.xml"/>
                <file name="inline_content">${exam.now} or formatted ${exam.now:dd.MM.yyyy'T'HH:mm:ss}</file>
            </e:fl-set>
        </e:then>
    </e:example>
    <e:example name="Empty command can be used to clear dir">
        <e:given>
            Clear dir filled by previous example
            <e:fl-show dir="dir"/>
        </e:given>
        <e:then print="true">
            <e:fl-set dir="dir"/>
        </e:then>
    </e:example>
</div>