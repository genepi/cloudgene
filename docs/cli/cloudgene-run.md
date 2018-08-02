# `cloudgene run`


Executes a Cloudgene workflow on the command line. All input parameters of a workflow can be set through command-line paramters.

## Command

```bash
cloudgene run <app_or_filename> <params> [--conf <hadoop_conf_dir>] [--user <hadoop_username>][--no-logging] [--no-output]
```
## Parameters

| Parameter                 | Required | Description |
| --- | --- | --- |
| `<app_or_filename>` | yes | The location of a cloudgene.yaml file or the id of an installed application. |
| `<params>` | yes | All input parameters of a workflow have to be set through the commandline. For example, if a input-paramter with id `input` was defined and is required, then the corresponding commandline-paramter is `--input <value>` |
| `--conf <hadoop_conf_dir>` | no | Path to Hadoop configuration folder (e.g. `/etc/hadoop/conf`)) |
| `--user <hadoop_username>` | no | Execute Hadoop steps on behalf of this username (default: **cloudgene**) |
| `--output` | no | Define a custom output folder (default: **./job_id**). |
| `--no-logging` | no | Don’t stream logging messages to terminal. |
| `--no-output` | no | Don’t stream output to terminal. |
| `--force` | no | Force Cloudgene to reinstall application in HDFS even if it already installed. |

## Examples
