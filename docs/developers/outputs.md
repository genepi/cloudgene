# Outputs

Output-Parameters can be used as placeholder for ouput-folders or output-files (HDFS or local). Cloudgene automatically creates this folders and enables downloading the files through the web interface.

Output parameters are defined in the `outputs` section where each parameter is defined by an unique `id`, a textual `description` and a `type`.

```yaml
workflow:
  outputs:
    - id: output
      description: Output Folder
      type: hdfs-folder
      mergeOutput: false
      download: true
      zip: false
```

The value of the parameter can be referenced by `$id` in the workflow. For example:

```yaml
workflow:
  steps:
    - name: Name Step1
      cmd: /bin/touch $output/new-file.txt
  outputs:
    - id: output
      description: Output Folder
      type: local-folder
      mergeOutput: false
      download: true
      zip: false
```


## Types

At the moment the following types of input parameter are supported:

#### `local-file`

#### `local-folder`

#### `hdfs-file`

#### `hdfs-folder`


## Downloading
If `download` is set to true, the file or folder can be downloaded (default: **true**).


## Intermediate results
If `temp` is set to true, this folder or file will be deleted automatically when the job is finished (default: **false**).


## Zip
If `zip` is set to true, all files in a hdfs-folder or local-folder are automatically compressed into a zip file (default: **true**).


## Merging output
If `mergeOutput` is set to true, all files in a hdfs-folder where merged into a single file (default: **true**). If `removeHeader` is set to true, the header of each file is removed and the merged file has a single header. default: **true**).
