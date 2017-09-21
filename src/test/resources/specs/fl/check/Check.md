# Check directory content
## `<e:fl-check dir="dir"/>`

***!!! Only XML comparing are supported !!!***

<div>
    <e:summary/>
    <e:given>
        <e:fl-set dir="dir">
            <file name="create_date.xml" from="data/create.xml"/>
            <file name="relatively_big_file" from="data/test.xml"/>
            <file name="empty_file"/>
            <file name="not_empty_file">
            <![CDATA[
                <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                <data>some file content</data>
            ]]>
            </file>
        </e:fl-set>
    </e:given>
    <e:example name="regexp">
        <e:then>
            <e:fl-check dir="dir">
                <file name="create_date.xml">
                    <data>
                        <datetime>2017-05-09T13:00:00</datetime>
                        <targetings>
                            <targeting>
                                <client>429072</client>
                                <combined_template_id>83512</combined_template_id>
                                <tip_id>512</tip_id>
                                <weight>0.9</weight>
                                <expiration_datetime>\d\d\d\d</expiration_datetime>
                                <start_datetime>2017-09-21</start_datetime>
                                <params>
                                    <param>
                                        <name>telephone</name>
                                        <value>88005555551</value>
                                    </param>
                                </params>
                            </targeting>
                        </targetings>
                    </data>
                </file>
                <file name="relatively_big_file" from="data/test.xml"/>
                <file name="empty_file"/>
                <file name="not_empty_file"/>
            </e:fl-check>
        </e:then>
    </e:example>
    <e:example name="Happy-path">
            <e:then print="true">
                <e:fl-check dir="dir">
                    <file name="empty_file"/>
                    <file name="relatively_big_file"/>
                    <file name="not_empty_file">
                        <data>some file content</data>
                    </file>
                </e:fl-check>
            </e:then>
        </e:example>
        <e:example name="Surplus file" status="ExpectedToFail">
            <e:then print="true">
                <e:fl-check dir="dir">
                    <file name="not_empty_file">
                        <data>some file content</data>
                    </file>
                </e:fl-check>
            </e:then>
        </e:example>
        <e:example name="Missing file" status="ExpectedToFail">
            <e:then print="true">
                <e:fl-check dir="dir">
                    <file name="empty_file"/>
                    <file name="relatively_big_file"/>
                    <file name="missing_file"/>
                    <file name="not_empty_file">
                        <data>some file content</data>
                    </file>
                </e:fl-check>
            </e:then>
        </e:example>
        <e:example name="Wrong file content" status="ExpectedToFail">
            <e:then print="true">
                <e:fl-check dir="dir">
                    <file name="empty_file"/>
                    <file name="relatively_big_file"/>
                    <file name="not_empty_file">
                        <data>another content was expected</data>
                    </file>
                </e:fl-check>
            </e:then>
        </e:example>
        <e:example name="All previous checks together" status="ExpectedToFail">
            <e:then print="true">
                <e:fl-check dir="dir">
                    <file name="relatively_big_file"/>
                    <file name="missing_file"/>
                    <file name="not_empty_file">
                        <data>another content was expected</data>
                    </file>
                </e:fl-check>
            </e:then>
        </e:example>
</div>