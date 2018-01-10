# BashCommand step

Cloudgene supports the execution of executable binaries.

!!! tip
    All HDFS inputs are automatically exported to the local filesystem and can be used without manual exportation in any executable binary.

## Parameters

| Parameter | Required | Description |
| --- | --- | --- |
| `cmd` | yes | The command that should be executed |
| `bash` | no | Enables or disable Bash specific features like pipes and loops (default: **false**) |
| `stdout` | no | Use stdout as step's output (default: **false**) |

## Examples

### Print message to using `/bin/echo`

This example shows how to forward stdout directly to the output of a step in order to display it the web-application.


```yaml
name: cmd example
version: 1.0
workflow:
  steps:
    - name: Print text to terminal
      cmd: /bin/echo $message
      stdout: true
  inputs:
    - id: message
      description: Message
      type: text
```


### Write message to a file using stdout streaming

This example shows how to use Bash specific features by setting the `bash` property to `true`:

```yaml
name: bash example
version: 1.0
workflow:
  steps:
    - name: Write text to file using pipes
      cmd: /bin/echo $message > $output
      bash: true
  inputs:
    - id: message
      description: Message
      type: text
  outputs:
    - id: output
      description: Output File
      type: local_file
```

### Working with HDFS files

All HDFS inputs are automatically exported to the local filesystem and can be used without manual exportation. In this example we use the `cat` command to show the content of an `hdfs-file` input parameter:

```yaml
name: hdfs example
version: 1.0
workflow:
  steps:
    - name: Print text to file using pipes
      cmd: /bin/cat $input
      stdout: true
  inputs:
    - id: input
      description: Output File
      type: hdfs_file
```
