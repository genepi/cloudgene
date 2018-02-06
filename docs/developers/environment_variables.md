# Environment Variables

Cloudgene supports several environment variables that can be used in your `cloudgene.yaml` files to get informations about the application itself and the submitted job.

## Requirements
| Variable | Description |
| --- | --- |
| `${hadoop_installed}` |  |
| `${docker_installed}` | |
| `${r_markdown_installed}` | |

## Application

| Variable | Description |
| --- | --- |
| `${app_local_folder}` | The path of the application directory.<br>This is the directory where your `cloudgene.yaml` file is located. |
| `${app_hdfs_folder}` | The path of the application HDFS directory.<br>This is the directory where you should put meta files. |
| `${local_app_folder}` | **Deprecreated**. Please use `${app_local_folder}` |
| `${hdfs_app_folder}` | **Deprecreated** Please use `${app_hdfs_folder}` |
| `${workdir}` | **Deprecreated**. Please use `${app_local_folder}` |

## Job

| Variable | Description |
| --- | --- |
| `${job_id}` | The id of the submitted job. |
| `${job_local_output}` | The workspace folder of the submitted job. |
| `${job_local_temp}` | The folder of the submitted job that can be used for temporary files.<br>After the job is completed all files within this folder are deleted automatically. |
| `${job_hdfs_output}` | The workspace HDFS folder of the submitted job in. |
| `${job_hdfs_temp}` | The HDFS folder of the submitted job that can be used for temporary files.<br>After the job is completed all files within this folder are deleted automatically. |


## User

| Variable | Description |
| --- | --- |
| `${user_username}` | The username of the user who submitted the job. |
| `${user_mail}` | The email address of the user who submitted the job. |


## Example

```yaml
name: print username
version: 1.0.0
workflow:
  steps:
    - name: Say hello
	    cmd: /bin/echo hey ${user_username}
	    stdout: true
```
