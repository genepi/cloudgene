# RMarkdown step

To give the user feedback on the results of an executed workflow, it is important to summarize them in user-friendly reports containing plots, tables and other visualizations. Cloudgene supports the integration of R scripts that can be formatted using RMarkdown in order to render HTML sites.

## Parameters

| Parameter | Required | Description |
| --- | --- | --- |
| `rmd2` | yes | The filename of your RMarkdown script (*.Rmd) |
| `params` | no | Input parameters that can be used inside the RMarkdown script (e.g. input files) |
| `output` | yes | The name and location of the rendered HTML page. |

## Examples

```yaml
name: r markdown example
version: 1.0
workflow:
  steps:
    - name: Running report script
      rmd2: report.Rmd
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
