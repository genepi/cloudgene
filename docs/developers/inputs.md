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
| `help` | no | Contains the link to a help page for this parameter (default: **empty**).<br>If a link is provided, a icon appears nearby the label:<br> ![](/images/inputs/help.png)
|


## Input Controls

At the moment the following types of input parameters are supported to build forms:

| Types | Control |
| --- | --- |
| [`local_file`](#local-files-and-folder) <br> [`hdfs_file`](#hdfs-files-and-folder) | ![](/images/inputs/file.png) |
| [`local_folder`](#local-files-and-folder) <br> [`hdfs_folder`](#hdfs-files-and-folder) | ![](/images/inputs/folder.png) |
| [`text`](#text) | ![](/images/inputs/text.png) |
| [`number`](#number) | ![](/images/inputs/number.png) |
| [`list`](#list) | ![](/images/inputs/list.png)
| [`app_list`](#app_list) | ![](/images/inputs/list.png)
| [`checkbox`](#checkbox) | ![](/images/inputs/checkbox.png) |

### `local_file`

Creates a file-upload field where one file can be selected and uploaded.

- uploaded file will be copied into local workspace
- contains absolute path to the uploaded file
- all input files will be deleted after job run

### `local_folder`

Creates a file-upload field where multiple files can be selected and uploaded.

- uploaded files will be copied into local workspace
- contains absolute path to the folder which contains all uploaded files
- all input files will be deleted after job run

### `hdfs_file`

Creates a file-upload field where file can be selected and uploaded.

- uploaded file will be imported into HDFS
- contains path to the imported file into the HDFS filesystem
- all input files will be deleted after job run

### `hdfs_folder`

Creates a file-upload field where one or multiple files can be selected and uploaded.

- uploaded files will be imported into HDFS
- contains path to the HDFS folder where all imported files were stored
- all input files will be deleted after job run

### `text`

Creates an input-field where a text can be entered by the user.

- TODO: format? length

### `number`

Creates an input-field where a number can be entered by the user.

- TODO: format? min, max, step? check on clientside needed

### `list`

Creates a drop-down list with different options. The `values` property contains key/value pairs of the available options in the list:

```yaml
- id: list
  description: Input List
  type: list
  values:
    keya: Value A
    keyb: Value B
```


### `checkbox`

Creates a checkbox with two different states. The `values` property contains values that are used if the checkbox is either selected (`true`) or unselected (`false`);

```yaml
- id: checkbox
  description: Input Checkbox
  type: checkbox
  value: false
  values:
    true: valueTrue
    false: valueFalse  
```

### `app_list`

Creates a drop-down list where the user can select an application. The property `category` is optional and can be used to display only applications with the provided category. The value of this parameter contains the **Application Link** of the selected application.

```yaml
- id: refData
  description: Reference Data
  type: app_list
  category: ref-data
```

Learn more about [Application Links](/tutorials/application-links) and their advantages.

## Layout and Groups

### `label`

Creates a label to display instructions or to separate input controls.

```yaml
- id: mylabel
  description: This is a label
  type: label
```

### `group`

- TODO
