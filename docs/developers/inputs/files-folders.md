# Files and Folders

At the moment the following filesystems are supported:

## Local Fileystem

### File

Creates a file-upload field where one file can be selected and uploaded.

```yaml
- id: image
  description: Image
  type: local_file
```

- uploaded file will be copied into local workspace
- contains absolute path to the uploaded file
- all input files will be deleted after job run


### Folder

Creates a file-upload field where multiple files can be selected and uploaded.

```yaml
- id: image
  description: Images
  type: local_folder
```


- uploaded files will be copied into local workspace
- contains absolute path to the folder which contains all uploaded files
- all input files will be deleted after job run

## HDFS (Apache Hadoop)

### File

Creates a file-upload field where file can be selected and uploaded.

```yaml
- id: image
  description: Images
  type: hdfs_file
```

- uploaded file will be imported into HDFS
- contains path to the imported file into the HDFS filesystem
- all input files will be deleted after job run

### Folder

Creates a file-upload field where one or multiple files can be selected and uploaded.

```yaml
- id: image
  description: Images
  type: hdfs_folder
```

- uploaded files will be imported into HDFS
- contains path to the HDFS folder where all imported files were stored
- all input files will be deleted after job run

## Filetypes

The `accept` attribute specifies the types of files that the server accepts. Per default all files can be selected and uploaded.

```yaml
- id: image
  description: Image
  type: local_file
  accept: .jpg
```

To specify more than one file, separate the values with a comma (e.g. `accept: .jpg, .gif`). Most of the browsers are not able to handle two dots in the extension (e.g. `.tar.gz`). Please use `.gz`.
