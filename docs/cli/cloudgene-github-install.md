# `cloudgene github-install`

Installs an application from a GitHub repo to your local repository.

**This command is deprecated. Please use `cloudgene install user/repo`**

## Command

```bash
cloudgene github-install <owner>/<repository>[/<subdir>]
```
## Parameters

| Parameter | Required | Description |
| --- | --- | --- |
| `<repository>` | yes | The GitHub repository of the application |

## Examples

Install application from GitHub repository `lukfor/hello-cloudgene`:

```bash
cloudgene github-install lukfor/hello-cloudgene
```

Install application from a subdirectory in GitHub repository `genepi/cloudgene-examples`:

```bash
cloudgene github-install genepi/cloudgene-examples/fastqc
```

Install a certain version (tag or release):

```bash
cloudgene github-install lukfor/hello-cloudgene@v1.2.0
```
