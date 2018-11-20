# Inputs

Input parameters can be used to define which files or information the enduser has to provide. For example, if you have a workflow that analyzes a CSV file, an input parameter can be defined to ask the user to upload his or her data file.

Cloudgene supports POSIX compatible filesystems (e.g. Linux or OS X) and the *Hadoop Distributed File System* (HDFS) as well as basic input fields for numbers or strings. On the basis of these input-parameters the Cloudgene web interface is created dynamically.

Input parameters are defined in the `inputs` section where each parameter is defined by an unique `id`, a textual `description` and a `type`.

```yaml hl_lines="5 6 7"
name: input-example
version: 1.0
workflow:
  inputs:
    - id: param1
      description: Description of parameter 1
      type: number
    - id: param2
      description: Description of parameter 2
      type: text
```

The value of the parameter can be referenced by `$id` in the workflow.

```yaml hl_lines="6"
name: input-example
version: 1.0
workflow:
  steps:
    - name: Name Step1
      cmd: /bin/echo Value of Parameter 1 $param1
      stdout: true
  inputs:
    - id: param1
      description: Description of parameter
      type: number
```
![](/images/inputs/example.png)




## Properties

These properties define the basic behaviour of an input parameter::

| Property | Required | Description |
| --- | --- | --- |
| `id` | yes | Defines a id for the parameter |
| `description` | yes | This text serves as label for the input field. |
| `type` | yes | One of the following [types](#input-controls) |
| `value` | no | Defines the default value of this parameter. This value is preselected in the job submission form (default: **empty**). |
| `visible` | no | Defines if the input control is visible or hidden (default: **true**). |
| `required` | no | Defines if the parameter is mandatory or can be submitted empty by the user (default: **true**). |
| `details` | no | Prints more details about  (default: **empty**). |
| `help` | no | Contains the link to a help page for this parameter (default: **empty**).<br>If a link is provided, a icon appears nearby the label:<br> ![](/images/inputs/help.png)
|
