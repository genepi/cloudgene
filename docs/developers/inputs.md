# Inputs

Input parameters can be used to define which files or information the enduser has to provide. For example, if you have a workflow that analyzes a CSV file, an input parameter can be defined to ask the user to upload his or her data file. Cloudgene supports POSIX compatible filesystems (e.g. Linux or OS X) and the *Hadoop Distributed File System* (HDFS) as well as basic input fields for numbers or strings. On the basis of these input-parameters the Cloudgene web interface is created dynamically.

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

The value of the parameter can be referenced by `$id` in the workflow. For example:

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

Cloudgene creates an user-interface based on the defined input parameters:

![](/images/inputs/example.png)

## General properties

These properties define the basic behaviour of an input parameter and are independent of its type:

| Property | Required | Description |
| --- | --- | --- |
| `id` | yes |  |
| `description` | yes | |
| `type` | yes | |
| `value` | no | Defines the default value of this parameter. This value is preselected in the job submission form (default: **empty**). |
| `visible` | no | |
| `required` | no | Defines if the parameter is mandatory or can be submitted empty by the user (default: **true**). |

## Types

At the moment the following types of input parameters are supported:


### local-file

![](/images/inputs/file.png)


### local-folder

![](/images/inputs/folder.png)

### hdfs-file

![](/images/inputs/file.png)


### hdfs-folder

![](/images/inputs/folder.png)

### text

![](/images/inputs/text.png)

### number

![](/images/inputs/number.png)

### list

![](/images/inputs/list.png)

```yaml hl_lines="4 5 6"
- id: list
  description: Input List
  type: list
  values:
    keya: Value A
    keyb: Value B       
```

### checkbox

![](/images/inputs/checkbox.png)

```yaml hl_lines="5 6 7"
- id: checkbox
  description: Input Checkbox
  type: checkbox
  value: false
  values:
    true: valueTrue
    false: valueFalse  
```

### label

### group
