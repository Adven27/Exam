# More examples with tables

<div>
    <e:summary/>
    <e:example name="Using of vars and EL">
        <e:given print="true">
            <span>Concordion variable age = <span c:set="#age">99</span></span>    
            <e:db-set table="PERSON" cols="NAME, AGE, BIRTHDAY, ID=1..10">
                <row>Andrew                                     , 30      , {{now "yyyy-MM-dd"}}</row>
                <row>Carl                                       , {{age}} , {{now "yyyy-MM-dd" plus="day 1"}}</row>
                <row>' untrimmed string with commas, inside it ', {{NULL}}, {{date "1951-05-14"}}</row>
            </e:db-set>
        </e:given>
        <e:then print="true">
            <e:db-check table="PERSON" cols="NAME, AGE, BIRTHDAY">
                <row>Andrew                                     , 30      , {{now "yyyy-MM-dd"}}</row>
                <row>  Carl                                     , {{age}} , {{now "yyyy-MM-dd" plus="day 1"}}</row>
                <row>' untrimmed string with commas, inside it ', {{NULL}}, {{dateFormat (date "1951-05-14") "yyyy-MM-dd"}}</row>
            </e:db-check>
        </e:then>
    </e:example>
    <e:example name="Check within tolerance of expected timestamp" status="ExpectedToFail">
        <e:given print="true">
            <e:db-set table="PERSON" cols="NAME, BIRTHDAY, ID=1..10">
                <row>withinNow pass, {{now plus="5 min"}}</row>
                <row>withinNow fail, {{now plus="5 min"}}</row>
                <row>within pass   , {{now}}</row>
            </e:db-set>
        </e:given>
        <e:then print="true">
            <e:db-check table="PERSON" cols="NAME, BIRTHDAY">
                <row>withinNow pass, !{within 10min}</row>
                <row>withinNow fail, !{within 1min}</row>
                <row>within pass   , !{within 25hours}{{now plus="1 day"}}</row>
            </e:db-check>
        </e:then>
    </e:example>
    <e:example name="Check with regex" status="ExpectedToFail">
        <e:given print="true">
            <e:db-set table="PERSON" cols="NAME, AGE, ID=1..10">
                <row>'', 30</row>
            </e:db-set>
        </e:given>
        <e:then print="true">
            <p>Check that NAME and BIRTHDAY are "NOT NULL" and AGE is a digit:</p>
            <e:db-check table="PERSON" cols="NAME, AGE, BIRTHDAY">
                <row>!{regex}.*, !{number}, !{notNull}</row>
            </e:db-check>
        </e:then>
    </e:example>
    <e:example name="Check with order by" status="ExpectedToFail">
        <e:given print="true">
            <e:db-set table="PERSON" cols="NAME, AGE, ID=1..10">
                <row>A, 2</row>
                <row>B, 1</row>
            </e:db-set>
        </e:given>
        <e:then print="true">
            <p>
                By default, db-check compares datasets sorted by all columns from "cols" attribute.
                This works fine in most cases. However in case of using "!{regex}" pattern as field value, 
                sorting of actual and expected datasets may give different results and false-negative fails like this:
            </p>
            <e:db-check table="PERSON" cols="NAME, AGE">
                <row>!{regex}.*, 1</row>
                <row>!{regex}.*, 2</row>
            </e:db-check>
            <p>
                In order to fix this, order and columns for sorting may be set explicitly: 
            </p>
            <e:db-check table="PERSON" cols="NAME, AGE" orderBy="AGE, NAME">
                <row>!{regex}.*, 1</row>
                <row>!{regex}.*, 2</row>
            </e:db-check>
        </e:then>
    </e:example>
    <e:example name="Date and Timestamp">
        <e:given print="true">
            <e:set var="beforePrev" value="{{now minus='2 d'}}"/> 
            <e:set var="prev" value="{{now minus='1 d'}}"/> 
            <e:set var="currentTime" value="{{now}}"/> 
            <e:db-set table="TYPES" cols="DATE_TYPE, TIMESTAMP_TYPE, DATETIME_TYPE, ID=1..10">
                <row>{{beforePrev}}, {{currentTime}}, {{currentTime}}</row>
                <row>{{prev}}, {{currentTime}}, {{currentTime}}</row>
                <row>{{currentTime}}, {{currentTime}}, {{currentTime}}</row>
            </e:db-set>
        </e:given>
        <e:then print="true">
            <e:db-check table="TYPES" cols="ID=1..10, DATE_TYPE, TIMESTAMP_TYPE, DATETIME_TYPE" orderBy="DATE_TYPE">
                <row>  !{within 3d}{{now}}, !{within 5s}{{now}}, !{within 1s}{{currentTime}}</row>
                <row>{{today minus="1 d"}},     {{currentTime}}, {{currentTime}}</row>
                <row>            {{today}}, !{within 5s}{{now}}, !{within 1s}{{currentTime}}</row>
            </e:db-check>
        </e:then>
    </e:example>
</div>