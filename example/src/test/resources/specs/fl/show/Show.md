# Show directory content
## `<e:fl-show dir="dir"/>`

<div>
    <e:summary/>
    <e:given>
        Given dir <code c:echo="dir"/>
    </e:given>
    <e:example name="Empty dir">
        <e:then print="true">
            <e:fl-show dir="dir"/>
        </e:then>
    </e:example>
    <e:example name="Not empty dir">
        <e:given>
            Given file <code c:set="#name">some_file</code> <span c:assertTrue="addFile(#name)">add to dir.</span>
        </e:given>
        <e:then print="true">
            <e:fl-show dir="dir"/>
        </e:then>
    </e:example>
</div>