# Stop Daemon

Stops the daemon process of Cloudgene server started with [`clougene-daemon`](/daemon/start-daemon)

## Command

```bash
sudo cloudgene-daemon -u <username> -a stop -j <path_to_jvm> [-d]
```

## Parameters

| Parameter | Required | Description |
| --- | --- | --- |
| `-u <username>` | yes | Cloudgene performs port binding (e.g. 443,80) as root and then switch identity to this non-privileged user. (default: **hadoop**) |
| `-j <path_to_jvm>` | yes | Full path to the installed Java VM (e.g. /opt/jdk1.7.0_25/). Detected automatically if not set. |
| `-d` | no | Enable debug mode: print anything and log everything on commandline. |
