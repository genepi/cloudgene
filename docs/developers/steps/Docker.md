# Docker step

Cloudgene supports the execution of commands inside a docker container.

## Parameters

| Parameter | Required | Description |
| --- | --- | --- |
| `type` | yes | has to be `docker` |
| `image` | yes | Docker image name inclusive version (e.g. broadinstitute/gatk:4.beta.6) |
| `cmd` | yes | The command that should be executed |

## Examples

```yaml
name: docker example
version: 1.0
workflow:
  steps:
    - name: Run command in docker container
      type: docker
      image: broadinstitute/gatk:4.beta.6
      cmd:  ./gatk-launch --list
```
