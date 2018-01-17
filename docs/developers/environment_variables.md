# Environment Variables

Cloudgene supports several environment variables that can be used in your `cloudgene.yaml` files to get informations about the used application or the submitted job.

## Application

| Variable | Description |
| --- | --- |
| `${local_app_folder}` |  |
| `${hdfs_app_folder}` |  |

## Job

| Variable | Description |
| --- | --- |
| `${job_id` |  |
| `${job_local_output}` |  |
| `${job_local_temp}` |  |
| `${job_hdfs_output}` |  |
| `${job_hdfs_temp}` |  |


## User

| Variable | Description |
| --- | --- |
| `${user_username}` |  |
| `${user_mail}` |  |


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
