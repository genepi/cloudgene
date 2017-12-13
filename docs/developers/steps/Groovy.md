# Groovy step

In addition to the tasks described in the previous sections, user-defined tasks can be integrated directly by implementing a Groovy function. No compilation is needed and your script can be included directly.

!!! danger
    This step is currently in alpha and interfaces/methods can be changed in future releases.


## Parameters

| Parameter | Required | Description |
| --- | --- | --- |
| `type` | yes | Type has to be `groovy` |
| `script` | yes | The filename of your Groovy script (*.groovy) |

## Examples

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
      value: John Lennon
```

#### step.groovy

Your Groovy script has to implement a method called `run` which can use the `context` to communicate with Cloudgene:

```groovy
import genepi.hadoop.common.WorkflowContext

def run(WorkflowContext context) {

	def name = context.get("name");

	for (int i = 1; i <= 10; i++){
		context.ok("Hello ${name}! I am Groovy Step Nr. " + i);
	}

	return true;
}
```

## Learn more

Learn more about how to read/write files and execute external processes:

- [The Groovy Development Kit](http://groovy-lang.org/groovy-dev-kit.html)
- [Calling Other Processes From Groovy](https://coderwall.com/p/nswp1q/calling-other-processes-from-groovy)
