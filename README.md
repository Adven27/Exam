[![Get automatic notifications about new "exam" versions](https://www.bintray.com/docs/images/bintray_badge_bw.png)](https://bintray.com/adven27/exam/exam?source=watch)

[![Build Status](https://travis-ci.org/Adven27/Exam.svg?branch=master)](https://travis-ci.org/Adven27/Exam)
[ ![Download](https://api.bintray.com/packages/adven27/exam/exam/images/download.svg?version=1.1.1) ](https://bintray.com/adven27/exam/exam/1.1.1/link)

# Exam
[Concordion](https://github.com/concordion/concordion) extension 

## Getting started
### 1) Install

Maven
```xml
<repositories>
    <repository>
      <id>jcenter</id>
      <url>https://jcenter.bintray.com/</url>
    </repository>
</repositories>  
```
```xml
<dependency>
    <groupId>org.adven.concordion.ext</groupId>
    <artifactId>exam</artifactId>
    <version>1.1.1</version>
</dependency>
```

Gradle
```groovy
repositories {
    jcenter()
}

testCompile "org.adven.concordion.ext:exam:1.1.1"
```
### 2) Use

For detailed info, [see original tutorial](http://concordion.org/tutorial/java/markdown/)

Specs.java
```java
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
```html
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
