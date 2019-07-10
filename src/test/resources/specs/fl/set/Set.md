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
                <file name="content_from_external_file" from="data/test.xml" autoFormat="true" lineNumbers="true"/>
                <file name="inline_content">{{now}} or formatted {{now "dd.MM.yyyy'T'HH:mm:ss"}}</file>
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
    <e:example name="Files will be created in sub directories">
        <e:given>
            Fill directory with files and sub directories with files
            <e:fl-set dir="dir">
                <file name="empty_file"/>
                <file name="subdir/file_content">{{now}} or formatted {{now "dd.MM.yyyy'T'HH:mm:ss"}}</file>
                <file name="subdir/file_content2">{{now}} or formatted {{now "dd.MM.yyyy'T'HH:mm:ss"}}</file>
                <file name="subdir/subsubdir/file_content">{{now}} or formatted {{now "dd.MM.yyyy'T'HH:mm:ss"}}</file>
            </e:fl-set>
        </e:given>
        <e:then print="true">
            <e:fl-show dir="dir"/>
        </e:then>
    </e:example>
</div>