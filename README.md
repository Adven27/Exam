[![Build Status](https://travis-ci.org/Adven27/Exam.svg?branch=master)](https://travis-ci.org/Adven27/Exam)
# Exam
[Concordion](https://github.com/concordion/concordion) extension 

## Getting started
### 1) Install

Add to build.gradle
```
repositories {
  maven { url "https://jitpack.io" }
}    

dependencies {
  testCompile ('com.github.Adven27:Exam:master-SNAPSHOT')
}
```
### 2) Use

For detailed info, [see original tutorial](http://concordion.org/tutorial/java/markdown/)

Specs.java
```
@RunWith(ConcordionRunner.class)
@ConcordionOptions(declareNamespaces = {"c", "http://www.concordion.org/2007/concordion", "e", ExamExtension.NS})
public class Specs {
    @Extension
    private final ExamExtension exam = new ExamExtension().
            rest().end().
            db().end();
}
```

Specs.md
```
#Some markdown header

<div>
    <e:example name="My dummy example">
        <e:given>
          <e:db-set table="PERSON" cols="NAME, AGE">
              <row>Andrew,30</row>
              <row>Carl,20</row>
          </e:db-set>
        </e:given>
        <e:post url="relative/url">
          <e:case desc="Happy-path">        
            <body>
                {"exact": "ok", "template": 1}
            </body>
            <expected>
                {"exact": "ok", "template": "!{number}"}
            </expected>
          </e:case>      
          <e:case desc="Expected to fail">
            <body>
                {"exact": "not ok", "template": "not number"}
            </body>
            <expected>
                {"exact": "ok", "template": "!{number}"}
            </expected>
          </e:case>
        </e:post>
    </e:example>
</div>
  ```
For more info, [see live spec](https://adven27.github.io/Exam/specs/Specs.html)
