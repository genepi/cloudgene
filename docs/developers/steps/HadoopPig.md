# HadoopPig step

Apache PIG scripts can be integrated as tasks in order to transform datasets created by other Hadoop jobs in a SQL like style.

## Parameters

| Parameter | Required | Description |
| --- | --- | --- |
| `pig` | yes |  |
| `params` | no |  |

## Examples

The following example shows how to integrate a pig script:

```yaml
name: Hadoop PIG
version: 1.0
workflow:
  steps:
    - name: Running pig script
      pig: filter_results.pig
      params: -param input=$hdfs_input
              -param output=$hdfs_output
  inputs:
    - id: hdfs_input
      description: HDFS-Input
      type: hdfs_folder
  outputs:
    - id: hdfs_output
      description: HDFS-Output
      type: hdfs_folder
```
