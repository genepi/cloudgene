# Control patterns


Some analytic workflows have a dynamic behaviour - for example, skipping a step based on the user input. Thus, this kind of workflow can not be represented by a static sequence of steps. To overcome this issue, WDL understands also some basic directives, which can be used to create steps. By using conditions and loops inside the WDL file the steps of a workflow can be constructed in a dynamic way based on the user input.

!!! important
    Each directive starts with # and **has to be** at beginning of a line.

## If condition

You can use every input parameter (in this example `$choice`) to build logical conditions. More about conditions and syntax examples can be found in the [Velocity manual](http://velocity.apache.org/engine/1.7/user-guide.html#if-elseif-else).

```yaml
name: if example
version: 1.0
workflow:
  steps:
#if ($choice == "yes")
    - name: Yes Step
      cmd: /bin/echo yes select
      stdout: true
#end

#if ($choice == "no")
    - name: No Step
      cmd: /bin/echo no select
      stdout: true
#end
  inputs:
    - id: choice
      description: Select a value
      values:
        no: No
        yes: Yes
      type: list
```

## Loops

You can use loops to add steps to your workflow dynamically:

```yaml
name: loop example
version: 1.0
workflow:
  steps:
#foreach($i in [1..10])
    - name: Step $i
      cmd: /bin/echo I am step number $i
      stdout: true
#end
```
