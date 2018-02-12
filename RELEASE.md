## Release Procedure ##

### Checkout master ###

Update your repository and run all tests with 'mvn test'. If an error is raised, it needs to be fixed and committed before going to the next step.

### Update version number

Update version according Sematic Versioning:

> Given a version number MAJOR.MINOR.PATCH, increment the:
>
> - MAJOR version when you make incompatible API changes,
> - MINOR version when you add functionality in a backwards-compatible manner, and
> - PATCH version when you make backwards-compatible bug fixes.
>
> Additional labels for pre-release and build metadata are available as extensions to the MAJOR.MINOR.PATCH format.
>
> https://semver.org/


We track our current version in
- `pom.xml`
- `src/main/java/cloudgene/mapred/Main.java`
- `src/main/html/webapp/package.json`.

Set the new version without the `v` prefix.

Then, commit and push the changes:

    git commit -m 'Prepare release 1.x.x'

**This should be the last commit before the release.**

### Create the release on GitHub

 Create a new release with tag `v1.x.x` (prefix `v`) and description including the major points and the changelog. The name of the new release has to be `1.x.x`.

 **Travis creates all assemblies and uploads them as assets to the new release.**

### Update install.cloudgene.io

The install script should be updated with the latest release version. Update `CLOUDGENE_VERSION=1.x.x` in [installer script](https://github.com/genepi/cloudgene-installer/blob/master/index.html).

**GitHub deploys the script to http://install.cloudgene.io**

### Update Docker image

The Dockerfile should be updated with the latest release version. Update `ENV CLOUDGENE_VERSION=1.x.x` in [Dockerfile](https://github.com/genepi/cloudgene-docker/blob/master/Dockerfile).

**Travis tests the image and DockerHub deploys it.**
