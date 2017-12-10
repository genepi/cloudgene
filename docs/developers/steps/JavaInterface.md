# JavaInterface step

In addition to the tasks described in the previous sections, user-defined tasks can be integrated directly in Java by implementing a defined interface. This has the advantage that the program code runs in the same instance as Cloudgene instead of creating a new process for every task. Thus, no command-line wrapper has to be written because the class has direct access to all input and output parameters.

Moreover, the task has much more capabilities to communicate with the workflow manager in order to transmit status updates and detailed error messages.

## Interface
