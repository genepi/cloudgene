# BashCommand step

Cloudgene supports the execution of executable binaries. Since all HDFS inputs paths of a non Hadoop task are automatically exported to the local filesystem, no additional steps are needed to work with this files.

## Parameters

| Parameter | Required | Description |
| --- | --- | --- |
| `cmd` | yes | The command that should executed in this step |

## Example

The following example illustrates the syntax:

    steps:
      - name: Running diff
        cmd: diff $hdfs_output1 $hdfs_output2
