# RMarkdown Docker step

**Work in progress**

## Parameters

| Parameter | Required | Description |
| --- | --- | --- |
| `rmd` | yes | The filename of your RMarkdown script (*.Rmd) |
| `params` | no | Input parameters that can be used inside the RMarkdown script (e.g. input files) |
| `output` | yes | The name and location of the rendered HTML page. |
| `image` | no | Custom Docker image. |

## Examples

### Example 1

```yaml
name: r markdown example
version: 1.0
workflow:
  steps:
    - name: Running report script
      type: rmd_docker
      rmd: report.Rmd
      params: $dataset
      output: ${report}.html
  inputs:
    - id: dataset
      description: Dataset
      type: hdfs_file
  outputs:
    - id: report
      description: Report
      type: local_file
```

### Example 2

Run a report script only when Docker is installed:

```yaml
name: rmd-docker
version: 1.0
workflow:
  steps:
#if (${docker_installed} == "true")
    - name: Running RMD in Docker Container
      type: rmd_docker
      rmd: report.Rmd
      output: ${report}.html
#end

  outputs:
    - id: report
      description: Report
      type: local_file
```
