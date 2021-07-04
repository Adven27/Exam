# Exam - [Concordion](https://github.com/concordion/concordion) extension

---

### Tutorials


## Database plugin

---

DbPlugin is a wrapper around DbUnit library and enables to set up and verify DB state.   

### Usage

Add dependency:

    testImplementation "io.github.adven27:exam-db:5.0.1"

Configure and attach plugin to ExamExtension:

<pre class="java doc-code">public class Specs extends AbstractSpecs {
    @Override
    protected ExamExtension init() {
        return new ExamExtension(
                new DbPlugin(
                        "org.postgresql.Driver", //driver class
                        "jdbc:postgresql://localhost:5432/postgres", //jdbc url 
                        "postgres", // user
                        "postgres" // password
                )
        );
    }
}</pre>

#### Commands:

<table class="table table-sm">

<tr><td colspan="3"> <h6>Apply datasets: </h6> </td></tr>

<tr>
<td class="align-middle"> <a c:run="concordion" href="db/DbSet.html">db-set</a> </td>
<td class="align-middle"> Creates and applies DbUnit dataset for specified table </td>
<td class="align-middle"> <pre class="xml doc-code">
<![CDATA[<e:db-set table="person" cols="id=1..10, name, birthday, created={{now}}">
    <e:row>Bob, {{date '01.01.2001' format='dd.MM.yyyy'}}</e:row>
    <e:row> Ed, {{date '02.02.2002' format='dd.MM.yyyy'}}</e:row>
</e:db-set>]]>
</pre> </td>
</tr>

<tr>
<td class="align-middle"> <a c:run="concordion" href="db/DbExecute.html">db-execute</a> </td>
<td class="align-middle"> Applies specified DbUnit datasets </td>
<td class="align-middle"> <pre class="xml doc-code">
<![CDATA[ <e:db-execute datasets="/data/db/adam.xml, /data/db/bob.json, /data/db/carl/person.csv"/> ]]>
</pre> </td>
</tr>

<tr>
<td class="align-middle"> <a c:run="concordion" href="db/DbClean.html">db-clean</a> </td>
<td class="align-middle"> Cleans specified tables with DELETE_ALL DbUnit operation </td>
<td class="align-middle"> <pre class="xml doc-code">
<![CDATA[ <e:db-clean tables="person, person_fields"/> ]]>
</pre> </td>
</tr>

<tr><td colspan="3"> <h6>Verify database state: </h6> </td></tr>

<tr>
<td class="align-middle"> <a c:run="concordion" href="db/DbCheck.html">db-check</a> </td>
<td class="align-middle"> Creates DbUnit dataset and verifies that it matches specified database table </td>
<td class="align-middle"> <pre class="xml doc-code">
<![CDATA[<e:db-check table="person" cols="id=1..10, name, birthday, created={{now}}">
    <e:row>Bob, {{date '01.01.2001' format='dd.MM.yyyy'}}</e:row>
    <e:row> Ed, {{date '02.02.2002' format='dd.MM.yyyy'}}</e:row>
</e:db-check>]]>
</pre> </td>
</tr>

<tr>
<td class="align-middle"> <a c:run="concordion" href="db/DbVerify.html">db-verify</a> </td>
<td class="align-middle"> Verifies that database matches state described in DbUnit datasets </td>
<td class="align-middle"> <pre class="xml doc-code">
<![CDATA[ <e:db-verify datasets="/data/db/adam.xml, /data/db/bob.json, /data/db/carl/person.csv"/> ]]>
</pre> </td>
</tr>

<tr><td colspan="3"> <h6>Debug database state: </h6> </td></tr>

<tr>
<td class="align-middle"> <a c:run="concordion" href="db/DbShow.html">db-show</a> </td>
<td class="align-middle"> Creates DbUnit dataset files and prints content of specified database table </td>
<td class="align-middle"> <pre class="xml doc-code">
<![CDATA[<e:db-show table="person" saveToResources="/data/db/person.xml"/>]]>
</pre> </td>
</tr>

</table>

## Message queue plugin

---

Add dependency:

```groovy
testImplementation "io.github.adven27:exam-mq:5.0.1"
```

Configure and attach plugin to ExamExtension:

```java
public class Specs extends AbstractSpecs {
    @Override
    protected ExamExtension init() {
        return new ExamExtension(
                new MqPlugin(

                )
        );
    }
}
```

#### Set up queue state

- [<e:mq-purge name="queueAliasName"/>]( mq/MqPurge.html)
- [<e:mq-send name="queueAliasName">]( mq/MqSend.html)

#### Verify queue state

- [<e:mq-check name="queueAliasName">]( mq/MqCheck.html)
