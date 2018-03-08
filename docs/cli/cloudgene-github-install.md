# `cloudgene github-install`

Installs an application from a GitHub repo to your local repository.

## Command

```bash
cloudgene github-install <owner>/<repository>[/<subdir>] [--name <id>] [--update]
```
## Parameters

| Parameter | Required | Description |
| --- | --- | --- |
| `<repository>` | yes | The GitHub repository of the application |
| `--name <id>` | no | Use a specific id for this application |
| `--update` | no | Reinstall application if it is already installed.  |

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
