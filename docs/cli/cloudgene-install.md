# `cloudgene install`

Installs an application from a zip file or a cloudgene.yaml file to your local repository.

## Command

```bash
cloudgene install <location>
```
## Parameters

| Parameter | Required | Description |
| --- | --- | --- |
| `<location>` | yes | The location of the application. This could be a URL to zip or yaml file, a github repository or the filename of a local zip or yaml file.|

## Examples

### Http Urls

Install application from a URL:

```bash
cloudgene install http://myserver.com/myapplication.zip
```


### GitHub

Install application from GitHub repository `lukfor/hello-cloudgene`:

```bash
cloudgene install github://lukfor/hello-cloudgene
```

Install application from a subdirectory in GitHub repository `genepi/cloudgene-examples`:

```bash
cloudgene install github://genepi/cloudgene-examples/fastqc
```

Install a certain version (tag or release):

```bash
cloudgene install github://lukfor/hello-cloudgene@v1.2.0
```
