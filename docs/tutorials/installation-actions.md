# Using InstallActions to import HDFS files

**InstallActions allow you to execute user defined actions after the installation process of an application. This allows you for example to import files into HDFS.**

In this tutorial we create an application that contains an InstallAction to import a file called `metafile.txt` into HDFS.

## Example

```ansi
app-installation
├── app-installation.yaml
├── print-hdfs-file.groovy
├── metafiles
|   └── metafile.txt
└── README.md
```


**cloudgene.yaml**

```yaml
name: app-installation
version: 1.0.0
installation:
  - import:
      source: ${local_app_folder}/metafiles/metafile.txt
      target: ${hdfs_app_folder}/metafiles/metafile.txt
workflow:
  steps:
    - name: print content of hfs file
      type: groovy
      script: print-hdfs-file.groovy
      file: ${hdfs_app_folder}/metafiles/metafile.txt
```

**print-hdfs-file.groovy**

```groovy
import genepi.hadoop.common.WorkflowContext
import genepi.hadoop.HdfsUtil

def run(WorkflowContext context) {

	def hdfs = context.getConfig("file");
	def tempFile = context.getLocalTemp()+"/file.txt";

	//export
	HdfsUtil.get(hdfs, tempFile);

	def content = new File(tempFile).text;
	context.ok(content);

	return true;
}
```

## InstallAction

```yaml
clougene install app-installation app-installation.yaml
cloudgene server
```
