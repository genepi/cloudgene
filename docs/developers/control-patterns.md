# Control patterns


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
