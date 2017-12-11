# BashCommand step

Cloudgene supports the execution of executable binaries. Since all HDFS inputs paths of a non Hadoop task are automatically exported to the local filesystem, no additional steps are needed to work with this files.

## Parameters

| Parameter | Required | Description |
| --- | --- | --- |
| `cmd` | yes | The command that should be executed |
| `bash` | no | Enables or disable Bash specific features like pipes and loops (default: **false**) |
| `stdout` | no | Use stdout as step's output (default: **false**) |

## Examples

The following examples illustrate the syntax.

### Print message using /bin/echo

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

We have to set `stdout` to `true` in order to see the messages in the web-application as a step output.

### Write message to a file using bash features

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
      type: local-file
```

### HDFS inputs

Cloudgene takes care of all file staging operation. Thus, all HDFS inputs are automatically exported to the local filesystem and can be used without manual exportation. In this example wir use the `cat` command to show the content of an hdfs file:

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
      type: hdfs-file
```
