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
| `--docker` | no | Start a Hadoop Cluster inside a Docker container and execute all Hadoop steps on this cluster. |
| `--image <docker_image>` | no | Use a custom Docker image for the Hadoop Cluster (default: **seppinho/cdh5-hadoop-mrv1**) |
| `--no-logging` | no | Don’t stream logging messages to terminal. |
| `--no-output` | no | Don’t stream output to terminal. |

## Examples

### Execute workflow without Hadoop support

```bash
cloudgene install hello-cloudgene http://cloudgene.uibk.ac.at/downloads/hello-cloudgene
cloudgene run hello-cloudgene
```

### Execute workflow on the Namenode of a Hadoop Cluster

Cloudgene uses the default configuration of your Hadoop Cluster. If it is installed on the Hadoop Namenode you can run it without additional flags:

```bash
cloudgene install hello-cloudgene http://cloudgene.uibk.ac.at/downloads/hello-cloudgene
cloudgene run hello-cloudgene
```
### Execute workflow on a remote Hadoop Cluster

```bash
cloudgene install hello-cloudgene http://cloudgene.uibk.ac.at/downloads/hello-cloudgene
cloudgene run cloudgene.yaml --host <remote-ip>
```

Cloudgene executes your MapReduce steps on the remote cluster. You can use the `--user` flag to set the username which should be used to execute your job (e.g. it uses the HDFS directory of this user for all files).

### Execute workflow on an ad-hoc Hadoop Cluster using Docker

```bash
cloudgene install hello-cloudgene http://cloudgene.uibk.ac.at/downloads/hello-cloudgene
cloudgene run cloudgene.yaml --docker
```
