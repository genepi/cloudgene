# Groovy step

User-defined tasks can also be integrated by implementing a [Groovy](http://groovy-lang.org/) function. Compared to the [Java Interface](/developers/steps/JavaInterface/), there is no compilation needed and a script can be included directly.

Groovy provides a simple but rich [IO API](http://groovy-lang.org/groovy-dev-kit.html#_working_with_io) to read/write files or to execute external processes.

!!! attention
    This step is currently under development and interfaces/methods can be changed in future releases.


## Parameters

| Parameter | Required | Description |
| --- | --- | --- |
| `type` | yes | Type has to be `groovy` |
| `script` | yes | The filename of your Groovy script (*.groovy) |

## Examples

### Hello World

The following example demonstrates how to execute a Groovy script and write feedback to Cloudgene:

#### cloudgene.yaml

```yaml
name: Groovy test
version: 1.0
workflow:
  steps:
    - name: Execte Groovy step
      type: groovy
      script: step.groovy
  inputs:
    - id: name
      description: Name
      type: text
      value: World
```

#### step.groovy

Your Groovy script has to implement a method called `run` which can use the `context` to communicate with Cloudgene:

```groovy
import genepi.hadoop.common.WorkflowContext

def run(WorkflowContext context) {

	// get input parameter 'name'
	def name = context.get("name");

	for (int i = 1; i <= 10; i++){
		// write to step output
		context.ok("Hello ${name}! I am Groovy Step Nr. " + i);
	}

	return true;
}
```

### Using external libraries

Cloudgene supports `@Grab` annotations to download missing dependencies automatically (e.g. from a Maven repository). The following example shows a Groovy script which parses a csv file using [GroovyCSV](https://github.com/xlson/groovycsv):

```groovy
@Grab('com.xlson.groovycsv:groovycsv:1.1')

import static com.xlson.groovycsv.CsvParser.parseCsv
import genepi.hadoop.common.WorkflowContext

def run(WorkflowContext context) {

	// get input parameter 'csv'
	def csvFilename = context.get('csv');

	def data = parseCsv(new FileReader(csvFilename), separator: '\t')
	for(line in data) {
		context.ok("$line.Name $line.Lastname")
	}

	return true;
}
```


## Learn more

Learn more about how to read/write files and execute external processes:

- [The Groovy Development Kit](http://groovy-lang.org/groovy-dev-kit.html)
- [Calling Other Processes From Groovy](https://coderwall.com/p/nswp1q/calling-other-processes-from-groovy)
