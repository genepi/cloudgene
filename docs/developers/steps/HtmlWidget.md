# HtmlWidget step

Renders visualizations into steps.

## Parameters

| Parameter | Required | Description |
| --- | --- | --- |
| `type` | yes | Type has to be `html_widget` |
| `template` | yes | The filename of your HTML template. |
| `scripts` | no | The filenames of your java script dependencies. |
| `stylesheet` | no | The filename of your css files. |
| `output` | no | If output is set, a html file is created. Otherwise, the html widget is used as output of the step. |


## Examples

The following example shows how to create a html widget using Datatables:

```yaml
id: html-widgets-example
name: HTML Widgets Example
version: 1.0.0
workflow:
  steps:
    - name: DataTable Widget
      type: html_widget
      template: datatables_widget.html
      scripts: https://code.jquery.com/jquery-3.3.1.js, https://cdn.datatables.net/1.10.19/js/jquery.dataTables.min.js, https://cdn.datatables.net/1.10.19/js/dataTables.bootstrap.min.js
      stylesheet: https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css, https://cdn.datatables.net/1.10.19/css/dataTables.bootstrap.min.css
      dataset1: ${dataset1}.json
```


datatables_widget.html:

```html
<table id="dataset1" class="table table-striped table-bordered" style="width:100%">
  <thead>
    <tr>
      <th>column 1</th>
      <th>column 2</th>
    </tr>
  </thead>
</table>

<script>

  $(document).ready(function() {
      $('#dataset1').DataTable( {
          data: ${dataset1},
          "columns": [
              { "data": "column 1" },
              { "data": "column 2" }
          ]
      } );
  } );

</script>
```
