# RMarkdown step

To give the user feedback on the results of an executed workflow, it is important to summarize them in user-friendly reports containing plots, tables and other visualizations. Cloudgene supports the integration of R scripts that can be formatted using RMarkdown in order to render HTML sites.

## Parameters

| Parameter | Required | Description |
| --- | --- | --- |
| `rmd` | yes |  |
| `params` | no |  |
| `output` | yes |  |

## Examples

```yaml
name: r markdown example
version: 1.0
workflow:
  steps:
    - name: Running report script
      rmd: report.Rmd
      params: $hdfs_output
      output: $report.html
```
