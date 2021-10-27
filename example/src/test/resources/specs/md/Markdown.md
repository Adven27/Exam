# Markdown example

## Commands

### [Mq example 1](-)

[send to myQueue](- "e:mq-send-exp=myQueue collapsable=true")
:
  ```json
  {"a" :  1}
  ```
: `formatAs=xml`
  ```xml
    <message>123</message>
  ```
: `formatAs=xml`
  _d1=1_
  _d2=2_
  ```xml
    <message>123</message>
  ```
: [`var1=1` `var2=2`](/data/mq/msg.json)
: `formatAs=json` [`var1=1` `var2=2`](/data/mq/msg.json)
: `formatAs=json`
  _d1=1_
  _d2=2_
  [`var1=1` `var2=2`][my message]

---

[check myQueue](- "e:mq-check=myQueue contains=exact collapsable=true layout=vertically")
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
: [`var1=1` `var2=2`](/data/mq/msg.json)
: `verifyAs=json` [`var1=1` `var2=2`](/data/mq/msg.json)
: `verifyAs=json` 
  _d1=1_
  _d2=2_ 
  [`var1=1` `var2=2`][my message]

### ~~Mq example 1~~

Next example:

### [Mq example 2](-)

[simplest form of mq-check](- "e:mq-check=myQueue")
:
  ```json
  {"a" :  1}
  ```
: [my message]

### ~~Mq example 2~~

Some text

## Bootstrap

### Notes

 **Note:** ddddd 

### Font Awesome

<i class="fas fa-puzzle-piece" aria-hidden="true"> </i> Puzzle Icon

### Cards

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


### Callouts

<div class="bd-callout bd-callout-info">Info</div>
<div class="bd-callout bd-callout-danger bg-warning text-danger shadow-lg">Warning</div>

[my message]: /data/mq/msg.json "fff"