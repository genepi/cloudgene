# Docker step

Cloudgene supports the execution of commands inside a docker container.

!!! tip
    All input and output parameters can be used inside the container since the workspace of a job is mounted automatically.


## Parameters

| Parameter | Required | Description |
| --- | --- | --- |
| `type` | yes | Type has to be `docker` |
| `image` | yes | Docker image name inclusive version (e.g. library/ubuntu or biocontainers/vcftools) |
| `cmd` | yes | The command that should be executed |
| `stdout` | no | Use stdout as step's output (default: **false**) |

## Examples

### VCFTools from biocontainers

The following example starts an image from biocontainers and executes `vcftools` to calculate allele frequencies:

```yaml
name: Allele Frequencies from VCF
version: 1.0
workflow:
  steps:
    - name: Calculate frequencies
      type: docker
      image: biocontainers/vcftools
      cmd:  vcftools --gzvcf $vcf --freq --out $output
  inputs:
    - id: vcf
      description: VCF File
      type: local_file
  outputs:
    - id: output
      description: Frequencies
      type: local_file
```
