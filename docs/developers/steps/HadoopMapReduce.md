# HadoopMapReduce step

Cloudgene supports both the execution of Hadoop jar files (written in Java) and the Hadoop Streaming mode (written in any other programming language).

## Parameters

| Parameter | Required | Description |
| --- | --- | --- |
| `jar` | yes |  |
| `params` | no |  |

## Examples

### Hadoop MapReduce jar

The following example shows how to execute a Hadoop jar file:

```yaml
name: Hadoop MapReduce
version: 1.0
workflow:
  steps:
    - jar: file.jar
      params: -param $input_param -param2 $output_param
  inputs:
    - id: input_param
      description: HDFS-Input
      type: hdfs_folder
  outputs:
    - id: output_param
      description: HDFS-Output
      type: hdfs_folder
```

### Hadoop MapReduce streaming mode

The following example shows how to integrate a streaming job:

```yaml
name: Hadoop MapReduce
version: 1.0
workflow:
  steps:
    - mapper: map.sh
      reducer: reducer.sh
      params: -input $input
              -output $output
              -file map.sh
              -file reducer.sh
  inputs:
    - id: input_param
      description: HDFS-Input
      type: hdfs_folder
  outputs:
    - id: output_param
      description: HDFS-Output
      type: hdfs_folder
```
