# Submit Job
The API allows to set several imputation parameters. Data need to be specified in vcf.gz or in the 23andMe data format (txt or zip).

## `POST /jobs/submit/{$app}`

The following parameters can be set:

| Parameter        | Values           | Default Value  |
| ------------- |:-------------| :-----|
| input-files      | /path/to/file |  |
| input-mode | qconly, imputation     | imputation   |
| input-files-source | file-upload, sftp, http     |  default: file-upload  |
| input-refpanel     | hapmap2, phase1, phase3, hrc.r1.1.2016, caapa      | phase3 |
| input-phasing | eagle, hapiur, shapeit      |  eagle  |
| input-population | eur, afr, asn, amr, sas, eas, AA, mixed      |  eur  |

## Examples

### curl

```sh
curl -H "X-Auth-Token: <your-API-token>" -F "input-files=@/path/to/genome.vcf.gz" -F "input-refpanel=hapmap2" -F "input-phasing=shapeit" https://imputationserver.sph.umich.edu/api/v2/jobs/submit/minimac
```

```sh
curl -H "X-Auth-Token: <your-API-token>" -F "input-files=http://warehouse.pgp-hms.org/warehouse/165ead886710368efd2d91624aa74f72+89/genome_v4_Full_20160602094938.txt" -F "input-files-source=http" -F "input-mode=qc" -F "input-mode=imputation" -F "input-refpanel=hrc.r1.1.2016" https://imputationserver.sph.umich.edu/api/v2/jobs/submit/minimac
```

```json
{
  "id":"job-20160504-155023",
  "message":"Your job was successfully added to the job queue.",
  "success":true
}
```

### Python

```python
import requests
import json

# imputation server url
url = 'https://imputationserver.sph.umich.edu/api/v2'

# add token to header (see authentication)
headers = {'X-Auth-Token' : token }

# submit new job
vcf = '/path/to/genome.vcf.gz';
files = {'input-files' : open(vcf, 'rb')}
r = requests.post(url + "/jobs/submit/minimac", files=files, headers=headers)
if r.status_code != 200:
    raise Exception('POST /jobs/submit/minimac {}'.format(r.status_code))

# print message
print r.json()['message']
print r.json()['id']
```
