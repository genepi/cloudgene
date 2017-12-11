# JavaJar step

Cloudgene supports the execution of jar files (written in Java).

## Parameters

| Parameter | Required | Description |
| --- | --- | --- |
| `jar` | yes | The filename of the jar (relativ to cloudgene.yaml file) |
| `params` | no | The paramters that you want to use in your jar |
| `runtime` | yes | Set it to `java` in order to execute the jar without the Hadoop classpath. |

## Examples

```yaml
name: java example
version: 1.0
workflow:
  steps:
    - name: Print text to terminal
      jar: my-jar-file.jar
      params: --param1 $file1
      runtime: java
  inputs:
    - id: file1
      description: Input File
      type: local-file
```
