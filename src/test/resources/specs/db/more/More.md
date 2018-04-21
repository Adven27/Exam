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
                <row>Andrew,     30, ${exam.now:yyyy-MM-dd}</row>
                <row>Carl,  ${#age}, ${exam.now+[day 1, m 1]:yyyy-MM-dd}</row>
                <row>' untrimmed string with commas, inside it ', ${#null}, ${exam.date(14.05.1951):yyyy-MM-dd}</row>
            </e:db-check>
        </e:then>
    </e:example>
</div>