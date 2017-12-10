# HadoopMapReduce step

Cloudgene supports both the execution of Hadoop jar files (written in Java) and the Hadoop Streaming mode (written in any other programming language).

## Parameters

| Parameter | Required | Description |
| --- | --- | --- |
| `jar` | yes |  |
| `params` | no |  |

## Examples

### Wordcount

The following example shows how easy a Hadoop jar file can be integrated:

    mapred:

      jar: file.jar
      params: -param $input_param -param2 $output_param

      inputs:

        - id: input_param
          description: Parameter Description
          type: hdfs-folder

        - ...

      outputs:

        - id: output_param
          description: HDFS-Output
          type: hdfs-folder

        - ...


## Hadoop MapReduce streaming mode

The following example shows how easy a streaming job can be integrated:

    mapred:

      mapper: map.sh
      reducer: reducer.sh
      params: -input $input
              -output $output
              -file map.sh
              -file reducer.sh

      inputs:

        - ...

      outputs:

        - ...
