## Development

### Building Webinterface

You need to install [Node.js](https://nodejs.org/) and [Grunt](https://gruntjs.com/):

- `cd src/main/html/webapp`

- `sudo npm install`

- `mkdir tmp`

- `grunt`

All created files can be found in folder `src/main/html/webapp/dist`.

### Building Webserver

You need [Maven](https://maven.apache.org/) to build all jar files and assemblies.

```
mvn install
```

**Before you build the Webserver, you have to build the Webinterface.**
All created files can be found in folder `target`
