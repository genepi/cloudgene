# Inputs

Input parameters can be used to define which files or information the enduser has to provide. For example, if you have a workflow that analyzes a CSV file, an input parameter can be defined to ask the user to upload his or her data file. On the basis of these input-parameters the Cloudgene web interface is created dynamically.

 Input parameters are defined in the `inputs` section where each parameter is defined by an unique `id`, a textual `description` and a `type`.

```yaml
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

```yaml
workflow:
  steps:
    - name: Name Step1
      exec: /bin/echo
      params: "Value of Parameter 1: $param1"
```

## Types

At the moment the following types of input parameter are supported:

### local-file

- set file filters

### local-folder

- set file filters

### hdfs-file

- set file filters

### hdfs-folder

- set file filters

### text

### number

### list

- values
- load values from file

### checkbox

### label

### group



## Default values

The property `value` defines the default value of this parameter. This value is preselected in the job submission form.


## Required parameters

The property `required` defines if the parameter is mandatory or can be submitted empty by the user.
