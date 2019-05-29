# More examples with tables

<div>
    <e:summary/>
    <e:example name="Using of vars and EL">
        <e:given print="true">
            <span>Concordion variable age = <span c:set="#age">99</span></span>    
            <e:db-set table="PERSON" cols="NAME, AGE, BIRTHDAY">
                <row>Andrew,     30, ${exam.now:yyyy-MM-dd}</row>
                <row>Carl,  ${#age}, ${exam.now+[day 1, m 1]:yyyy-MM-dd}</row>
                <row>' untrimmed string with commas, inside it ', ${#null}, ${exam.date(14.05.1951)}</row>
            </e:db-set>
        </e:given>
        <e:then print="true">
            <e:db-check table="PERSON" cols="NAME, AGE, BIRTHDAY">
                <row>Andrew,        30, ${exam.now:yyyy-MM-dd}</row>
                <row>  Carl,   ${#age}, ${exam.now+[day 1, m 1]:yyyy-MM-dd}</row>
                <row>' untrimmed string with commas, inside it ', ${#null}, ${exam.date(14.05.1951):yyyy-MM-dd}</row>
            </e:db-check>
        </e:then>
    </e:example>
    <e:example name="Check with regex" status="ExpectedToFail">
        <e:given print="true">
            <e:db-set table="PERSON" cols="NAME, AGE">
                <row>'', 30</row>
            </e:db-set>
        </e:given>
        <e:then print="true">
            <p>Check that NAME and BIRTHDAY are "NOT NULL" and AGE is a digit:</p>
            <e:db-check table="PERSON" cols="NAME, AGE, BIRTHDAY">
                <row>regex:.*, regex:\d+, regex:.*</row>
            </e:db-check>
        </e:then>
    </e:example>
    <e:example name="Check with order by" status="ExpectedToFail">
        <e:given print="true">
            <e:db-set table="PERSON" cols="NAME, AGE">
                <row>A, 2</row>
                <row>B, 1</row>
            </e:db-set>
        </e:given>
        <e:then print="true">
            <p>
                By default, db-check compares datasets sorted by all columns from "cols" attribute.
                This works fine in most cases. However in case of using "regex:" pattern as field value, 
                sorting of actual and expected datasets may give different results and false-negative fails like this:
            </p>
            <e:db-check table="PERSON" cols="NAME, AGE">
                <row>regex:.*, 1</row>
                <row>regex:.*, 2</row>
            </e:db-check>
            <p>
                In order to fix this, order and columns for sorting may be set explicitly: 
            </p>
            <e:db-check table="PERSON" cols="NAME, AGE" orderBy="AGE, NAME">
                <row>regex:.*, 1</row>
                <row>regex:.*, 2</row>
            </e:db-check>
        </e:then>
    </e:example>
</div>