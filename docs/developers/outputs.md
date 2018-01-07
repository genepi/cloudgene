# Outputs

Output parameters can be used as placeholders for folders or files which are created by steps. Cloudgene supports POSIX compatible filesystems (e.g. Linux or OS X) and the *Hadoop Distributed File System* (HDFS). Cloudgene creates and manages these folders for you as well as enables downloading the files through the web interface.

Output parameters are defined in the `outputs` section where each parameter is defined by an unique `id`, a textual `description` and a `type`.

```yaml hl_lines="5 6 7 8 9 10"
name: output-example
version: 1.0
workflow:
  outputs:
    - id: output
      description: Output Folder
      type: hdfs-folder
      mergeOutput: false
      download: true
      zip: false
```

The value of the parameter can be referenced by `$id` in the workflow. In this example, we use the `touch` command to create a file in an output folder:

```yaml hl_lines="6"
name: output-example
version: 1.0
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

## General properties

These properties define the basic behaviour of an output parameter and are independent of its type:

| Property | Required | Description |
| ---- | --- | --- |
| `id` | yes |  |
| `description` | yes | |
| `type` | yes |  |
| `download` | no | If `download` is set to true, the file or folder can be downloaded (default: **true**). |
| `adminOnly` | no | (default: **false**). |

## Types

At the moment the following types of output parameters are supported:

#### local-file

#### local-folder

#### hdfs-file

| Property | Required | Description |
| --- | --- | --- |
| `zip` | no | If `zip` is set to true, all files in a hdfs-folder or local-folder are automatically compressed into a zip file (default: **true**). |

#### hdfs-folder

| Property            | Required | Description |
| ------------------- | --- | --- |
| `mergeOutput`       | no | If `mergeOutput` is set to true, all files in a hdfs-folder are merged into a single file (default: **true**). |
| `zip`               | no | If `zip` is set to true, all files in a hdfs-folder or local-folder are automatically compressed into a zip file (default: **true**). |
| `removeHeader`      | no | If `removeHeader` is set to true, the header of each file is removed and the merged file has a single header. default: **true**). |
