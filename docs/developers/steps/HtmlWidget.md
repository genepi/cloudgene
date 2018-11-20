# HtmlWidget step

Renders visualizations into steps.

## Parameters

| Parameter | Required | Description |
| --- | --- | --- |
| `type` | yes | Type has to be `html_widget` |
| `template` | yes | The filename of your HTML template. |
| `scripts` | no | The filenames of your java script dependencies. |
| `stylesheet` | no | The filename of your css files. |

## Examples

The following example shows how to integrate LocusZomm:

```yaml
name: html-widgets
version: 1.0.0
workflow:
  steps:
    - name: LocusZoom
      type: html_widget
      template: locuszoom.html
      scripts: locuszoom/locuszoom.vendor.min.js, locuszoom/locuszoom.app.min.js
      stylesheet: locuszoom/locuszoom.css
```
