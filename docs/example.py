import requests
import json

# imputation server url
url = 'http://localhost:8082/api/v2'

# -------------------------------------

# authentication
data = {'username': 'lukfor', 'password': 'luki'}
r = requests.post(url + "/auth", data=data)
if r.status_code != 200:
    raise Exception('POST /auth/ {}'.format(r.status_code))

# read token
token = r.json()["token"]

# add token to header
headers = {'X-Auth-Token' : token }

# -------------------------------------

# get all jobs
r = requests.get(url + "/jobs", headers=headers)
if r.status_code != 200:
    raise Exception('GET /jobs/ {}'.format(r.status_code))

# print all jobs
for job in r.json():
    print('{} [{}]'.format(job['id'], job['state']))

# -------------------------------------

# submit new job
vcf = '/home/lukas/cloud/Shared/Genepi/Testdata/imputationserver/chr20.R50.merged.1.330k.recode.unphased.vcf.gz';
files = {'input-files' : open(vcf, 'rb')}
r = requests.post(url + "/jobs/submit/minimac", files=files, headers=headers)
if r.status_code != 200:
    raise Exception('POST /jobs/submit/minimac {}'.format(r.status_code))

# print message
print r.json()['message']
print r.json()['id']
