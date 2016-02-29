# manifest.yaml

## What is WDL?

One central idea behind this system is to build a generic approach to integrate and support new software programs and complex analysis workflows in a straightforward way. For this purpose we developed **WDL**, a domain language which enables a formal description of tasks and workflows. WDL is property-based and uses the data serialization syntax defined in the YAML specification. In comparison with other XML based languages, the YAML syntax is easier to use since XML is verbose and not easy to write. WDL was developed with the aim to support the usage of heterogeneous software components (MapReduce, R and unix command-line programs) and basic workflow control patterns.

* * *

## How to integrate an application into Cloudgene

First of all, a new program folder must be created in the apps folder of Cloudgene. This folder must contain a file called **cloudgene.yaml**. This file is also called the *Manifest File* and includes all information needed in order to install and execute your program. This folder contains also all other files that are needed by your MapReduce program. For example the jar file of a ready-to-use MapReduce program, a PIG script or some R-Markdown script:

![enter image description here][1]

* * *

## manifest.yaml

The file content starts with a simple header containing general information about the task/workflow followed by the description of input/output parameters as well as other relevant information that is necessary to execute the Hadoop job:

![enter image description here][2]

The MapReduce specific section starts with the definition of all needed input datasets and all derived datasets. Each of these datasets is represented by a parameter which is defined by a unique identifier, a textual description and a type. The data type defines the source of the used data: this can be either a local file, an HDFS file, a text or a number. All input parameters are existing resources and they can be set by the user during the submission process.

On the other hand, output parameters can be used as placeholders for output folders or output files (HDFS or local). If the generated dataset is a final result then the workflow designer can mark it as persistent. Intermediate datasets that are used to transfer data between tasks can be marked as temporary and will be deleted after the workflow execution. All output parameters represent non-existing resources that will be created by tasks. A task encapsulates a Hadoop job or any other script. The metadata of such a task describes the input and output interface and contains all details about the execution of the job or the script.

A simple example looks like this:

    name: tool-name
    description: tool-description
    category: tool-category
    version: 1.0
    website: http://www.my-website.com

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


* * *

### Input Parameters

On the basis of these input-parameters the Cloudgene web interface is created dynamically. Each parameter is defined by an id, a textual description and a type:

    inputs:

      - id: manifest
        description: Manifest File
        type: number
        value: 50
        required: true


The following table describes all properties of an input parameter in detail:

*TODO*

* * *

### Output Parameters

Output-Parameters can be used as placeholder for ouput-folders or output-files (HDFS or local). Cloudgene automatically creates this folders local and enables downloading this files through the web interface.

    outputs:

      - id: output
        description: Output Folder
        type: hdfs-folder
        mergeOutput: false
        download: true
        zip: false


Each of these parameters is defined by an id, a textual description and a type. The following table describes all properties of an output parameter in detail:

<div>
  <table class="table table-bordered table-striped">
    <thead>
      <tr>
        <th>
          Name
        </th>

        <th>
          Description
        </th>
      </tr>
    </thead>

    <tbody>
      <tr>
        <td>
          id
        </td>

        <td>
          The unique name of the output parameter. The value of the parameter can be referenced by $id in the parameter-list.
        </td>
      </tr>

      <tr>
        <td>
          description
        </td>

        <td>
          The description of the parameter which appears in the graphical user-interface.
        </td>
      </tr>

      <tr>
        <td>
          type
        </td>

        <td>
          hdfs-folder | hdfs-file | local-folder | local-file
        </td>
      </tr>

      <tr>
        <td>
          download
        </td>

        <td>
          if download is set to true, the file or folder can be downloaded (default: true).
        </td>
      </tr>

      <tr>
        <td>
          temp
        </td>

        <td>
          if temp is set to true, this folder or file will be deleted automatically when the job is run (default: false).
        </td>
      </tr>

      <tr>
        <td>
          zip
        </td>

        <td>
          if zip is set to true, all files in a hdfs-folder are stored in a zip file (default: true).
        </td>
      </tr>

      <tr>
        <td>
          mergeOutput
        </td>

        <td>
          if mergeOutput is set to true, all files in a hdfs-folder where merged into a single file (default: true).
        </td>
      </tr>

      <tr>
        <td>
          removeHeader
        </td>

        <td>
          if removeHeader is set to true, the header of each file is removed and the merged file has a single header. default: true).
        </td>
      </tr>
    </tbody>
  </table>
</div>

* * *

## Types of Task

At the moment Cloudgene supports the execution of several Hadoop technologies and the execution of executable binaries. This hybrid approach enables to build workflows by using existing software tools and by combining them with the power of MapReduce programs. In existing solutions, HDFS files must be exported manually to the local filesystem in order to use and analyze them in a non Hadoop software. Cloudgene solves this problem by supporting an automatic file staging which provides a direct way from HDFS to a command-line-program.

* * *

### Appache Hadoop MapReduce

Cloudgene supports both the execution of Hadoop jar files (written in Java) and the Hadoop Streaming mode (written in any other programming language). Moreover, PIG scripts can be integrated as tasks in order to transform datasets created by other Hadoop jobs in a SQL like style. This extension enables the usage of scripts based on SeqPig and BioPig which provide functions to analyse and transform a variety of NGS file formats.

**Hadoop MapReduce programs:**

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


**Hadoop MapReduce streaming mode:**

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


**Apache PIG:**

The following example shows how easy a pig script can be integrated:

    steps:
      - name: Running pig script
        pig: filter_results.pig
        params: -param input=$hdfs_output_tmp
                -param output=$hdfs_output


* * *

### Unix commands

Cloudgene supports the execution of executable binaries. Since all HDFS inputs paths of a non Hadoop task are automatically exported to the local filesystem, no additional steps are needed. The following example illustrates the syntax:

    steps:
      - name: Running diff
        exec: diff $hdfs_output1 $hdfs_output2


* * *

### Report generation using RMarkdown

To give the user feedback on the results of an executed workflow, it is important to summarize them in user-friendly reports containing plots, tables and other visualization methods. For this purpose Cloudgene supports the integration of R scripts that can be formatted using RMarkdown in order to render HTML sites.

    steps:
      - name: Running report script
        rmd: report.Rmd
        output: $report.html
        params: $hdfs_output


* * *

### Java Interface

In addition to the tasks described in the previous sections, user-defined tasks can be integrated directly in Java by implementing a defined interface. This has the advantage that the program code runs in the same instance as Cloudgene instead of creating a new process for every task. Thus, no command-line wrapper has to be written because the class has direct access to all input and output parameters. Moreover, the task has much more capabilities to communicate with the workflow manager in order to transmit status updates and detailed error messages. Please read this blog post to learn more about this topic.

* * *

## Workflows

A MapReduce workflow is a set of MapReduce jobs which should be executed in a well-defined order where jobs can use datasets generated from other jobs as their input. The simplest way to model such a workflow is to create a list of steps where each item depends on its forerunner. Assume that the workflow designer has created a list of tasks *t1*, *t2* and *t3*. During runtime the tasks are executed step by step that means *t2* depends on *t1* and *t3* depends on *t2*.

A simple example looks like this:

    mapred:

      steps:

        - name: Name Step1
          jar: snptest.jar
          params: -input $input -output $intermediate

        - name: Name Step2
          mapper: map-step2.sh
          reducer: reducer-step2.sh
          params: -input $intermediate -output $output

      inputs:

        - ...

      outputs:

        - ...


Different task types can be combined into one workflow in order to profit of the advantages of each technology. Such kind of workflow is illustrated on the example of an analysis pipeline used in Genome-wide association studies:

![enter image description here][3]

Finally, a workflow has the same properties as a task, therefore existing workflows can be included into new workflows in the same way as tasks. Please read this blog post to learn more about this topic.

* * *

## Control Patterns

Some analytic workflows have a dynamic behaviour - for example, skipping a step based on the user input. Thus, this kind of workflow can not be represented by a static sequence of steps. To overcome this issue, WDL understands also some basic directives, which can be used to create steps. By using conditions and loops inside the WDL file the steps of a workflow can be constructed in a dynamic way based on the user input. In addition, WDL provides a simple interface which can be implemented in a Java class in order to extend Cloudgene with new user defined functions. Currently, Cloudgene supports a variety of already implemented utilities which facilitate the creation of workflows in the field of Bioinformatics especially for NGS. This toolbox includes functions for data file determination as well as utilities for BAM and FASTQ files. The following listing shows an example to sort only unsorted BAM-files:

    - steps:

    #foreach ($bam in $bam_files.getFiles())
    #if (!$BamUtil.isSorted($bam))
      - jar: hadoop-bam.jar
        param: sort $bam
    #end
    #end

      - jar: variation-calling.jar
        param: \$bam_files $output_folder  


The introduction of directives opens a wide range of new possibilities: (1) basic input validation, (2) different data flows based on the input data, and (3) easier building of workflows by combining existing Hadoop programs without code modification.

* * *

 [1]: http://cloudgene.uibk.ac.at/wp-content/uploads/2014/09/cloudgene.png
 [2]: http://cloudgene.uibk.ac.at/wp-content/uploads/2014/09/cloudgene-2.png
 [3]: http://cloudgene.uibk.ac.at/wp-content/uploads/2014/09/cloudgene-3.png
