# Using InstallActions to import HDFS files

**InstallActions allow you to execute user defined actions after the installation process of an application. This allows you for example to import files into HDFS.**

## Installation-on-demand

When the user starts an application, Cloudgene checks if the application is already installed and may starts importing the needed files. This shortens the configuration process of a new application and ensures that no additional manual steps are needed when you run Cloudgene on a different Hadoop cluster.

## InstallActions

The installation process of an application is full configurable by using so called *InstallActions*. This actions can be defined in the `installation` section in the `cloudgene.yaml` file.

The simplest way to import a file during installation is to define an `import` action containing the filename in `source` and the HDFS folder in `target`:

```yaml
name: app-installation
version: 1.0.0
installation:
  - import:
      source: /local/file/metafile.txt
      target: hdfs-path/metafile.txt
```

To avoid hard-coded paths and to create full portable applications, we recommender to use the provided [environment variables](/developers/environment_variables.md):

```yaml
name: app-installation
version: 1.0.0
installation:
  - import:
      source: ${app_local_folder}/file.zip
      target: ${app_hdfs_folder}/content-of-zip-file
```

The environment variable `${app_local_folder}` is the path of the application directory (i.e. the directory where your `cloudgene.yaml` file is located). `${app_hdfs_folder}` points to directory in HDFS that is managed by Cloudgene and will be automatically deleted when you deinstall an application. This ensures, that files from removed applications don't litter your filesystem.

If the filename in `source` points to a folder, Cloudgene imports all files and subfolders into HDFS and keeps the folder structure:

```yaml
name: app-installation
version: 1.0.0
installation:
  - import:
      source: ${app_local_folder}/folder
      target: ${app_hdfs_folder}/folder
```

If the filename in `source` points to an archive file (ends with gz or zip), Cloudgene extracts the archive and imports all files and subfolders to the HDFS folder:

```yaml
name: app-installation
version: 1.0.0
installation:
  - import:
      source: ${app_local_folder}/file.zip
      target: ${app_hdfs_folder}/content-of-zip
```

Finally, Cloudgene supports also URLs (http and https) to files or archives:

```yaml
name: app-installation
version: 1.0.0
installation:
  - import:
      source: http://example.com/downloads/file.zip
      target: ${app_hdfs_folder}/folder
```

S3 Support: coming soon!

## Application Life Cycle and Reinstallation

TODO:

- Describe states of application:
    - `n/a`: no installation required
    - `on demand`: installation on next job run.
    - `completed`: installation completed
- How to force Reinstall:
    - `force` flag on commandline
    - webinterface: admin panel, applications, if app is in state `completed`, then click on delete icon near `completed`. new state is `on demand`. (screenshot)

## Example

```ansi
app-installation
├── app-installation.yaml
├── print-hdfs-file.groovy
├── metafiles
|   └── metafile.txt
└── README.md
```


### cloudgene.yaml

```yaml
name: app-installation
version: 1.0.0
installation:
  - import:
      source: ${app_local_folder}/metafiles/metafile.txt
      target: ${app_hdfs_folder}/metafiles/metafile.txt
workflow:
  steps:
    - name: print content of hfs file
      type: groovy
      script: print-hdfs-file.groovy
      file: ${app_hdfs_folder}/metafiles/metafile.txt
```

### print-hdfs-file.groovy

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

### Install and Testing

```yaml
cloudgene install app-installation app-installation.yaml
cloudgene server
```
