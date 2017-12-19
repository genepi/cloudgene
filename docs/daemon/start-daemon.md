# Start Daemon

Starts the Cloudgene server as a daemon process. By using JSVC its possible to perform port binding (e.g. 443,80) as root and then switch identity to a non-privileged user.

## Command

```bash
sudo cloudgene-daemon -u <username> -p <port> -a start -j <path_to_jvm> [-d]
```

## Parameters

| Parameter | Required | Description |
| --- | --- | --- |
| `-u <username>` | yes | Cloudgene performs port binding (e.g. 443,80) as root and then switch identity to this non-privileged user. (default: **hadoop**) |
| `-p <port>` | no | HTTP port on which the Cloudgene daemon should listen. (default: **8082**) |
| `-j <path_to_jvm>` | yes | Full path to the installed Java VM (e.g. /opt/jdk1.7.0_25/). Detected automatically if not set. |
| `-d` | no | Enable debug mode: print anything and log everything on commandline. |
