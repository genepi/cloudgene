# API v2.0

The REST APIs provide programmatic ways to submit new jobs and to download data from Michigan Imputation Server. The REST API identifies users using authentication tokens; responses are available in JSON. You need an active user account to use this API.

## Authentication

### POST /auth


### Examples
#### curl

```sh
curl -d "username=albert" -d "password=einstein"  https://imputationserver.sph.umich.edu/api/v2/auth
```

```json
{
  "message":"Login successfull.",
  "token":"your-API-token",
  "type":"plain",
  "success":true
}
```

#### Python

```python
import requests
import json

# imputation server url
url = 'http://localhost:8082/api/v2'

# authentication
data = {'username': 'albert', 'password': 'einstain'}
r = requests.post(url + "/auth", data=data)
if r.status_code != 200:
    raise Exception('POST /auth/ {}'.format(r.status_code))

# read token
token = r.json()["token"]
```

## List all jobs

### GET /jobs

### Examples
#### curl

```sh
curl -H "X-Auth-Token: your-API-token" https://imputationserver.sph.umich.edu/api/v2/jobs
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

#### Python

```python
import requests
import json

# imputation server url
url = 'http://localhost:8082/api/v2'

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

## Submit new job

### POST /jobs/submit/minimac

### Examples
#### curl

```sh
curl -H "X-Auth-Token: your-API-token" -F "input-files=@/home/lukas/cloud/Shared/Genepi/Testdata/imputationserver/chr20.R50.merged.1.330k.recode.unphased.vcf.gz" https://imputationserver.sph.umich.edu/api/v2/jobs/submit/minimac
```

```json
{
  "id":"job-20160504-155023",
  "message":"Your job was successfully added to the job queue.",
  "success":true
}
```

#### Python

```python
import requests
import json

# imputation server url
url = 'http://localhost:8082/api/v2'

# add token to header (see authentication)
headers = {'X-Auth-Token' : token }

# submit new job
vcf = '/home/lukas/cloud/Shared/Genepi/Testdata/imputationserver/chr20.R50.merged.1.330k.recode.unphased.vcf.gz';
files = {'input-files' : open(vcf, 'rb')}
r = requests.post(url + "/jobs/submit/minimac", files=files, headers=headers)
if r.status_code != 200:
    raise Exception('POST /jobs/submit/minimac {}'.format(r.status_code))

# print message
print r.json()['message']
print r.json()['id']
```

## Monitor job status

### /jobs/{id}/status

### Examples
#### curl

```sh
curl-H "X-Auth-Token: your-API-token" https://imputationserver.sph.umich.edu/api/v2/jobs/job-20160504-155023/status
```

```json
{
  "application":"Michigan Imputation Server (Minimac3 1.0.13 - Cloudgene 1.12.0)",
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

Todo:
- remove positionInQueue, outputParams, logs, deletedOn, application vs. applicationId, steps
- replace states with constants (e.g. RUNNING, WAITING, ...)

## Get job details

### GET /jobs/{id}/details

### Examples
#### curl

```sh
curl --H "X-Auth-Token: your-API-token" https://imputationserver.sph.umich.edu/api/v2/jobs/job-20160504-155023/details
```

```json
{
	"application": "Michigan Imputation Server (Experimental Mode) (Minimac3 1.0.13 - Cloudgene 1.12.0)",
	"applicationId": "minimac-admin",
	"deletedOn": -1,
	"endTime": 1462020562930,
	"executionTime": 86610091,
	"id": "job-20160429-084540",
	"logs": "logs/job-20160429-084540",
	"name": "job-20160429-084540",
  "positionInQueue": 0,
	"running": false,
	"startTime": 1461933952839,
	"state":,  
	"outputParams": [{
		"description": "Quality-Control Results",
		"files": [{
			"count": 10,
			"hash": "784c0243396e23221a8eb6d7efbad906",
			"name": "qcreport.html",
			"parameterId": 201692,
			"path": "job-20160429-084540/qcreport/qcreport.html",
			"size": "17 KB",
			"username": "admin"
		}],
		"format": "",
		"id": 201692,
		"input": false,
		"name": "qcreport",
		"type": "local-file",
		"zip": false
	}, {
		"description": "Imputation Report",
		"files": [{
			"count": 10,
			"hash": "50baaf1e09c098dae097065303816252",
			"name": "report",
			"parameterId": 201693,
			"path": "job-20160429-084540/report/report",
			"size": "1 KB",
			"username": "admin"
		}],
		"format": "",
		"id": 201693,
		"input": false,
		"name": "report",
		"type": "local-file",
		"zip": false
	}, {
		"description": "Imputation Results",
		"files": [{
			"count": 9,
			"hash": "3f993d442b62a432489cdeb0a1e6f2bd",
			"name": "chr_20.zip",
			"parameterId": 201695,
			"path": "job-20160429-084540/local/chr_20.zip",
			"size": "2 GB",
			"username": "admin"
		}],
		"format": "",
		"id": 201695,
		"input": false,
		"name": "local",
		"type": "local-folder",
		"zip": false
	}],
	"steps": [{
		"id": 38445,
		"logMessages": [{
			"message": "File Import successful. ",
			"time": 1462020565636,
			"type": 0
		}, {
			"message": "1 valid VCF file(s) found.\n\nSamples: 10080\nChromosomes: 20\nSNPs: 17220\nChunks: 7\nDatatype: phased\nReference Panel: hrc.r1.1.2016\nPhasing: eagle",
			"time": 1462020565636,
			"type": 0
		}],
		"name": "Input Validation",
		"up": null
	}, {
		"id": 38446,
		"logMessages": [{
			"message": "Execution successful.",
			"time": 1462020565636,
			"type": 0
		}, {
			"message": "<b>Statistics:<\/b> <br>Alternative allele frequency > 0.5 sites: 4,443<br>Reference Overlap: 100.00% <br>Match: 16,621<br>Allele switch: 0<br>Strand flip: 0<br>Strand flip and allele switch: 0<br>A/T, C/G genotypes: 0<br><b>Filtered sites:<\/b> <br>Filter flag set: 0<br>Invalid alleles: 0<br>Duplicated sites: 0<br>NonSNP sites: 0<br>Monomorphic sites: 564<br>Allele mismatch: 35<br>SNPs call rate < 90%: 0",
			"time": 1462020565636,
			"type": 0
		}, {
			"message": "Excluded sites in total: 599<br>Remaining sites in total: 16,621<br>",
			"time": 1462020565636,
			"type": 2
		}],
		"name": "Quality Control",
		"up": null
	}, {
		"id": 38447,
		"logMessages": [{
			"message": "Execution successful.",
			"time": 1462020565636,
			"type": 0
		}],
		"name": "Quality Control (Report)",
		"up": null
	}, {
		"id": 38448,
		"logMessages": [{
			"message": "<span class=\"badge badge-success\" style=\"width: 40px\">Chr 20<\/span><br><br><span class=\"badge\" style=\"width: 8px\">&nbsp;<\/span> Waiting<br><span class=\"badge badge-info\" style=\"width: 8px\">&nbsp;<\/span> Running<br><span class=\"badge badge-success\" style=\"width: 8px\">&nbsp;<\/span> Complete",
			"time": 1462020565636,
			"type": 0
		}],
		"name": "Pre-phasing and Imputation",
		"up": null
	}, {
		"id": 38449,
		"logMessages": [{
			"message": "Execution successful.",
			"time": 1462020565645,
			"type": 0
		}],
		"name": "Imputation (Report)",
		"up": null
	}, {
		"id": 38450,
		"logMessages": [{
			"message": "Exported data.",
			"time": 1462020565655,
			"type": 0
		}, {
			"message": "We have sent an email to <b>sebastian.schoenherr@uibk.ac.at<\/b> with the password.",
			"time": 1462020565655,
			"type": 0
		}],
		"name": "Data Compression and Encryption",
		"up": null
	}]
}
```
