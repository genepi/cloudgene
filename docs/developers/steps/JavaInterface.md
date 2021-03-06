# JavaInterface step

In addition to the tasks described in the previous sections, user-defined tasks can be integrated directly in Java by implementing a defined interface. This has the advantage that the program code runs in the same instance as Cloudgene instead of creating a new process for every task. Thus, no command-line wrapper has to be written because the class has direct access to all input and output parameters.

Moreover, the task has much more capabilities to communicate with the workflow manager in order to transmit status updates and detailed error messages.

## Interface

First, you have to create a new java project and include our genepi-hadoop library. We have a maven repository for that library:

```xml
<repository>
    <id>bintray-genepi-maven</id>
    <name>bintray</name>
    <url>https://dl.bintray.com/genepi/maven</url>
</repository>
```

The dependency is the following:

```xml
<dependencies>
    <dependency>
        <groupId>cloudgene</groupId>
        <artifactId>cloudgene-java-sdk</artifactId>
        <version>1.0.2</version>
    </dependency>
</dependencies>
```

Next, you have to extend `genepi.hadoop.common.WorkflowStep` and implement the run method :

```java
public class YourValidation extends CloudgeneStep {

    public boolean run(WorkflowContext context) {

        //parameters defined in yaml file
        String input = context.get("input");
        String output = context.get("output");

        //your application logic

        //some outputs
        context.ok("okey message");
        context.error("error message");
        context.message("...");
        context.warning("...");

        //true --> OK, false --> validation failed.
        return true;

    }
}
```

## cloudgene.yaml

After creating a jar archive for your project (for example by using maven), you can include your WorkflowStep into Cloudgene:

```yaml
id: java-interface-example
name: Java Interface Example
version: 1.0
workflow:
  steps:
    - name: Input Validation
      jar: your-jar-file.jar
      classname: your.package.YourValidation
  inputs:
    - id: input
      description: Input Parameter
      type: text
  outputs:
    - id: output
      description: Output Parameter
      type: loca_file
```

A Eclipse project including a maven file and a cloudgene.yaml file can be forked [here]().
