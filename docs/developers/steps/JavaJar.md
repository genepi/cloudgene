# JavaJar step

Cloudgene supports the execution of jar files (written in Java).

## Parameters

| Parameter | Required | Description |
| --- | --- | --- |
| `type` | yes | Type has to be `java` |
| `jar` | yes | The filename of the jar (relativ to cloudgene.yaml file) |
| `params` | no | The paramters that you want to use in your jar |

## Examples

### Execute Jar Archive

 The following example illustrates how to execute the default main-class of jar archive:

```yaml
name: java example
version: 1.0
workflow:
  steps:
    - name: Execute jar archive
      type: java
      jar: my-jar-file.jar
      params: --param1 $file1
  inputs:
    - id: file1
      description: Input File
      type: local_file
```
