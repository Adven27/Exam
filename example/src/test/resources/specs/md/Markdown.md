# Markdown example

## Commands

https://concordion.github.io/concordion/latest/spec/specificationType/markdown/Markdown.html
https://www.markdownguide.org/extended-syntax/#definition-lists

## Examples

### [Before each example](- "before")

- Purge queue **[myQueue](- "e:mq-purge=#TEXT")**.
- Clean tables: **[orders](- "e:db-clean=#TEXT")**.

### ~~Before each example~~

### MQ and DB support

#### [Mq example 1](-)

**Given**

| Empty |
|-------|
[[Orders list](- "e:db-set=orders")]

| Empty |
|-------|
[[](- "e:db-check=orders")]

**When**

- [send to myQueue](- "e:mq-send=myQueue collapsable=true")
-   ```json
    {
     "a": 1
    }
    ```
-   `formatAs=xml`
    ```xml
    <message>123</message>
    ```
-   `formatAs=xml`
    _d1=1_
    _d2=2_
    ```xml
     <message>123</message>
    ```
-  [`myVar1=1` `myVar2=2`](/data/mq/msg.json)
-   `formatAs=json`
    _d1=1_
    _d2=2_
    [`myVar1=1` `myVar2=2`][my message]

**Then**

[Events are sent to myQueue](- "e:mq-check=myQueue contains=exact collapsable=true layout=vertically")
:   
```json
  {"a" :  1}
```
: `verifyAs=xml` 
```xml
    <message>123</message>
```
: `verifyAs=xml`
_d1=1_
_d2=2_
```xml
    <message>123</message>
```
: [`myVar1=1` `myVar2=2`](/data/mq/msg.json)
: `verifyAs=json` 
_d1=1_
_d2=2_ 
[`myVar1=1` `myVar2=2`][my message]

| id  | client | driver   | status    | created               | updated   |
|-----|--------|----------|-----------|-----------------------|-----------|
| 1   | 111    | {{NULL}} | PLACED    | {{today}}             | {{today}} |
| 2   | 222    | 11       | COMPLETED | {{today minus='1 d'}} | {{today}} |
[[Set orders](- "e:db-set=orders")]

Orders updated:

| id        | client | driver     | status    | created               | updated      |
|-----------|--------|------------|-----------|-----------------------|--------------|
| !{number} | 111    | {{NULL}}   | PLACED    | {{today}}             | {{today}}    |
| !{string} | 222    | !{notNull} | COMPLETED | {{today minus='1 d'}} | !{within 2d} |
[[Check orders](- "e:db-check=orders awaitAtMostSec=2")]

#### ~~Mq example 1~~

Next example:

#### [Mq example 2](- "mq-2 c:status=ExpectedToFail")

[simplest form of mq-check](- "e:mq-check=myQueue")
:
  ```json
  {"a" :  1}
  ```
: [my message]

#### ~~Mq example 2~~

Some text

### Bootstrap

#### Notes

**Note:** some note

#### Font Awesome

<i class="fas fa-puzzle-piece" aria-hidden="true"> </i> Puzzle Icon

#### Cards

<div class="card">
  <div class="card-header">Header</div>
  <div class="card-body">
    DESCRIPTION
  </div>
  <div class="card-footer">footer</div>
</div>

---

<div class="card">
  <div class="card-header bg-info text-white">Info</div>
  <div class="card-body">
    DESCRIPTION
  </div>
</div>

#### Callouts

<div class="bd-callout bd-callout-info">Info</div>
<div class="bd-callout bd-callout-danger bg-warning text-danger shadow-lg">Warning</div>

[my message]: /data/mq/msg.json "fff"