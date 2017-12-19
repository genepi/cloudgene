# Job Status

## `/jobs/{id}/status`

## Examples

### curl

```sh
curl-H "X-Auth-Token: <your-API-token>" https://imputationserver.sph.umich.edu/api/v2/jobs/job-20160504-155023/status
```

```json
{
  "application":"Michigan Imputation Server (Minimac3 1.0.13 - Cloudgene 1.13.0)",
  "applicationId":"minimac",
  "deletedOn":-1,
  "endTime":1462369824173,
  "executionTime":0,
  "id":"job-20160504-155023",
  "logs":"",
  "name":"job-20160504-155023",
  "outputParams":[],
  "positionInQueue":0,
  "running":false,
  "startTime":1462369824173,
  "state":5
  ,"steps":[]
}
```
