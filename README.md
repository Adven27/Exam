[![Build Status](https://travis-ci.org/Adven27/Exam.svg?branch=master)](https://travis-ci.org/Adven27/Exam)
# Exam
concordion extension

Specs https://adven27.github.io/Exam/specs/Specs.html

```
repositories {
  maven { url "https://jitpack.io" }
}    

dependencies {
  testCompile ('com.github.Adven27:Exam:master-SNAPSHOT')
}

@RunWith(ConcordionRunner.class)
@ConcordionOptions(declareNamespaces = {"c", "http://www.concordion.org/2007/concordion", "e", ExamExtension.NS})
public class Specs {
    @Extension
    private final ExamExtension exam = new ExamExtension().
            dbTester("org.h2.Driver", "jdbc:h2:mem:test;INIT=CREATE SCHEMA IF NOT EXISTS SA\\;SET SCHEMA SA", "sa", "");

  ```
