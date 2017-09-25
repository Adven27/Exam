# File comparing details

<div>
    <e:summary/>
    <e:example name="Order in list">
        <e:given>
            <e:fl-set dir="dir">
                <file name="file" autoFormat="true"><![CDATA[
                    <?xml version="1.0" encoding="UTF-8"?>
                    <list>
                        <item>one</item>
                        <item>two</item>
                    </list>
                    ]]>
                </file>
            </e:fl-set>
        </e:given>
        <e:then print="true">
            <e:fl-check dir="dir">
                <file name="file"><![CDATA[
                    <?xml version="1.0" encoding="UTF-8"?>
                    <list>
                        <item>two</item>
                        <item>one</item>
                    </list>
                    ]]>
                </file>
            </e:fl-check>
        </e:then>
    </e:example>
    <e:example name="Tag order">
        <e:given>
            <e:fl-set dir="dir">
                <file name="file" autoFormat="true"><![CDATA[
                    <?xml version="1.0" encoding="UTF-8"?>
                    <data>
                        <one>one</one>
                        <two>two</two>
                    </data>
                    ]]>
                </file>
            </e:fl-set>
        </e:given>
        <e:then print="true">
            <e:fl-check dir="dir">
                <file name="file"><![CDATA[
                    <?xml version="1.0" encoding="UTF-8"?>
                    <data>
                        <two>two</two>
                        <one>one</one>
                    </data>
                    ]]>
                </file>
            </e:fl-check>
        </e:then>
    </e:example>
    <e:example name="Failed" status="fail">
        <e:given>
            <e:fl-set dir="dir">
                <file name="file" autoFormat="true"><![CDATA[
                    <?xml version="1.0" encoding="UTF-8"?>
                    <data>
                        <one>one</one>
                        <two>two</two>
                        <three>three</three>
                    </data>
                    ]]>
                </file>
            </e:fl-set>
        </e:given>
        <e:then print="true">
            <e:fl-check dir="dir">
                <file name="file"><![CDATA[
                    <?xml version="1.0" encoding="UTF-8"?>
                    <data>
                        <two>two</two>
                        <two>two</two>
                        <one>one</one>
                    </data>
                    ]]>
                </file>
            </e:fl-check>
        </e:then>
    </e:example>
</div>