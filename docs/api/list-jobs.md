# List Jobs

All running jobs can be returned as JSON objects at once.

## `GET /jobs`

## Examples

### curl

```sh
curl -H "X-Auth-Token: <your-API-token>" https://imputationserver.sph.umich.edu/api/v2/jobs
```

```json
[
  {
    "applicationId":"minimac",
    "executionTime":0,
    "id":"job-20160504-155023",
    "name":"job-20160504-155023",
    "positionInQueue":0,
    "running":false,
    "state":5
  },{
    "applicationId":"minimac",
    "executionTime":0,
    "id":"job-20160420-145809",
    "name":"job-20160420-145809",
    "positionInQueue":0,
    "running":false,
    "state":5
  },{
    "applicationId":"minimac",
    "executionTime":0,
    "id":"job-20160420-145756",
    "name":"job-20160420-145756",
    "positionInQueue":0,
    "running":false,
    "state":5
  }
]
```

### Python

```python
import requests
import json

# imputation server url
url = 'https://imputationserver.sph.umich.edu/api/v2'

# add token to header (see authentication)
headers = {'X-Auth-Token' : token }

# get all jobs
r = requests.get(url + "/jobs", headers=headers)
if r.status_code != 200:
    raise Exception('GET /jobs/ {}'.format(r.status_code))

# print all jobs
for job in r.json():
    print('{} [{}]'.format(job['id'], job['state']))
```

Todo:

- remove positionInQueue
- replace states with constants (e.g. RUNNING, WAITING, ...)
