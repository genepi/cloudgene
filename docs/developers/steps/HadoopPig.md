# HadoopPig step

Apache PIG scripts can be integrated as tasks in order to transform datasets created by other Hadoop jobs in a SQL like style.

## Parameters

| Parameter | Required | Description |
| --- | --- | --- |
| `pig` | yes |  |
| `params` | no |  |

## Examples

The following example shows how easy a pig script can be integrated:

    steps:
      - name: Running pig script
        pig: filter_results.pig
        params: -param input=$hdfs_output_tmp
                -param output=$hdfs_output
