# File comparing details

<div>
    <e:summary/>
    <e:given>
        <e:fl-set dir="dir">
            <file name="file"><![CDATA[
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <data>
                    <one>one</one>
                    <two>two</two>
                </data>]]>
            </file>
        </e:fl-set>
    </e:given>
    <e:example name="Tag order">
        <e:then print="true">
            <e:fl-check dir="dir">
                <file name="file"><![CDATA[
                    <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                    <data>
                        <two>two</two>
                        <one>one</one>
                    </data>]]>
                </file>
            </e:fl-check>
        </e:then>
    </e:example>
</div>