# Stop Daemon

## Command

```bash
sudo cloudgene-daemon -u <username> -a stop -j <path_to_jvm> [-d]
```

## Parameters

### `-u <username>`

Cloudgene performs port binding (e.g. 443,80) as root and then switch identity to this non-privileged user. (default: **hadoop**)

### `-j <path_to_jvm>`

Full path to the installed Java VM (e.g. /opt/jdk1.7.0_25/). Detected automatically if not set.

### `-d`

Enable debug mode: print anything and log everything on commandline.
