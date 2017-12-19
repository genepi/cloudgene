# Job Details

## `GET /jobs/{id}/details`

## Examples

### curl

```sh
curl --H "X-Auth-Token: <your-API-token>" https://imputationserver.sph.umich.edu/api/v2/jobs/job-20160504-155023/details
```

```json
{
	"application": "Michigan Imputation Server (Experimental Mode) (Minimac3 1.0.13 - Cloudgene 1.13.0)",
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
