# `cloudgene run`


Executes a Cloudgene workflow on the command line. All input parameters of a workflow can be set through command-line paramters.

## Command

```bash
cloudgene run <app_or_filename> <params> [--host <hadoop_cluster_ip>] [--user <hadoop_username>] [--docker] [--image <docker_image>] [--no-logging] [--no-output]
```
## Parameters

| Parameter                 | Required | Description |
| --- | --- | --- |
| `<app_or_filename>` | yes | The location of a cloudgene.yaml file or the id of an installed application. |
| `<params>` | yes | All input parameters of a workflow have to be set through the commandline. For example, if a input-paramter with id `input` was defined and is required, then the corresponding commandline-paramter is `--input <value>` |
| `--host <hadoop_cluster_ip>` | no | Execute all Hadoop steps on this Hadoop JobTracker (default: **use localhost as JobTracker**). |
| `--user <hadoop_username>` | no | Execute Hadoop steps on behalf of this username (default: **cloudgene**) |
| `--conf <HADOOP_CONF>` | no | Path to Hadoop configuration folder (e.g. /etc/hadoop/conf)) |
| `--output` | no | Define a custom output folder (*default: **./job_id**). |
| `--no-logging` | no | Don’t stream logging messages to terminal. |
| `--no-output` | no | Don’t stream output to terminal. |
| `--force` | no | Force Cloudgene to reinstall application in HDFS even if it already installed. |

## Examples

### Execute workflow without Hadoop support

```bash
cloudgene install hello-cloudgene http://www.cloudgene.io/downloads/hello-cloudgene
cloudgene run hello-cloudgene
```

### Execute workflow on the Namenode of a Hadoop Cluster

Cloudgene uses the default configuration of your Hadoop Cluster. If it is installed on the Hadoop Namenode you can run it without additional flags:

```bash
cloudgene install hello-cloudgene http://www.cloudgene.io/downloads/hello-cloudgene
cloudgene run hello-cloudgene
```
### Execute workflow on a remote Hadoop Cluster

```bash
cloudgene install hello-cloudgene http://www.cloudgene.io/downloads/hello-cloudgene
cloudgene run cloudgene.yaml --host <remote-ip>
```

Cloudgene executes your MapReduce steps on the remote cluster. You can use the `--user` flag to set the username which should be used to execute your job (e.g. it uses the HDFS directory of this user for all files).
