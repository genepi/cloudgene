import $ from 'jquery';
import 'popper.js';
import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
import '@fortawesome/fontawesome-free/css/all.css';
import 'components/core/layout/layout.css';
import List from 'can-list';

import 'helpers/helpers';

import template from './table.stache';
import JobDetails from 'models/job-details';

var jobs1 = {
  "count": 8,
  "success": 1,
  "failed": 0,
  "pending": 0,
  "waiting": 2,
  "running": 5,
  "canceld": 0,
  "data": [{
      "app": {
        "author": "",
        "category": "",
        "cluster": null,
        "deinstallation": [],
        "description": "Sleeping for 30 seconds",
        "id": "sleep",
        "installation": [],
        "manifestFile": "/home/lukas/sleep.yaml",
        "mapred": null,
        "name": "Sleep",
        "path": "/home/lukas",
        "properties": null,
        "source": "",
        "submitButton": "Submit Job",
        "version": "1.0",
        "website": "",
        "workflow": {
          "inputs": [{
            "accept": "",
            "adminOnly": false,
            "category": "",
            "description": "Seconds",
            "details": "",
            "fileOrFolder": false,
            "folder": false,
            "hdfs": false,
            "help": "",
            "id": "secs",
            "required": true,
            "type": "number",
            "typeAsEnum": "NUMBER",
            "value": "30",
            "values": null,
            "visible": true
          }],
          "onFailure": null,
          "outputs": [],
          "setup": null,
          "setups": [],
          "type": "sequence"
        }
      },
      "application": "Sleep 1.0",
      "applicationId": "sleep",
      "canceld": false,
      "complete": false,
      "currentTime": 1542205784830,
      "deletedOn": -1,
      "endTime": 1542205783225,
      "finishedOn": 0,
      "id": "job-20181114-152904-199",
      "name": "job-20181114-152904-199",
      "positionInQueue": 0,
      "priority": 21,
      "progress": -1,
      "running": true,
      "setupEndTime": 1542205744227,
      "setupRunning": false,
      "setupStartTime": 1542205744225,
      "startTime": 1542205753220,
      "state": 4,
      "submittedOn": 1542205744199,
      "user": {
        "fullName": "Lukas Forer",
        "id": 1,
        "mail": "ttt@dsad.com",
        "roles": ["admin"],
        "username": "admin"
      },
      "workspaceSize": "1 KB"
    }, {
      "app": {
        "author": "",
        "category": "",
        "cluster": null,
        "deinstallation": [],
        "description": "Sleeping for 30 seconds",
        "id": "sleep",
        "installation": [],
        "manifestFile": "/home/lukas/sleep.yaml",
        "mapred": null,
        "name": "Sleep",
        "path": "/home/lukas",
        "properties": null,
        "source": "",
        "submitButton": "Submit Job",
        "version": "1.0",
        "website": "",
        "workflow": {
          "inputs": [{
            "accept": "",
            "adminOnly": false,
            "category": "",
            "description": "Seconds",
            "details": "",
            "fileOrFolder": false,
            "folder": false,
            "hdfs": false,
            "help": "",
            "id": "secs",
            "required": true,
            "type": "number",
            "typeAsEnum": "NUMBER",
            "value": "30",
            "values": null,
            "visible": true
          }],
          "onFailure": null,
          "outputs": [],
          "setup": null,
          "setups": [],
          "type": "sequence"
        }
      },
      "application": "Sleep 1.0",
      "applicationId": "sleep",
      "canceld": false,
      "complete": false,
      "currentTime": 1542205784831,
      "deletedOn": -1,
      "endTime": 0,
      "finishedOn": 0,
      "id": "job-20181114-152919-174",
      "name": "job-20181114-152919-174",
      "positionInQueue": 1,
      "priority": 27,
      "progress": 0,
      "running": true,
      "setupEndTime": 1542205759203,
      "setupRunning": false,
      "setupStartTime": 1542205759201,
      "startTime": 0,
      "state": 1,
      "submittedOn": 1542205759174,
      "user": {
        "fullName": "Lukas Forer",
        "id": 1,
        "mail": "ttt@dsad.com",
        "roles": ["admin"],
        "username": "admin"
      },
      "workspaceSize": "366 bytes"
    }, {
      "app": null,
      "application": "Sleep 1.0",
      "applicationId": "sleep",
      "canceld": false,
      "complete": true,
      "currentTime": 1542205784854,
      "deletedOn": -1,
      "endTime": 0,
      "finishedOn": 1542205679776,
      "id": "job-20181114-152743-393",
      "name": "job-20181114-152743-393",
      "positionInQueue": -1,
      "priority": 0,
      "progress": -1,
      "running": false,
      "setupEndTime": 1542205663421,
      "setupRunning": false,
      "setupStartTime": 1542205663419,
      "startTime": 0,
      "state": 6,
      "submittedOn": 1542205663393,
      "user": {
        "fullName": "Lukas Forer",
        "id": 1,
        "mail": "ttt@dsad.com",
        "roles": ["admin"],
        "username": "admin"
      },
      "workspaceSize": "511 bytes"
    }, {
      "app": null,
      "application": "Michigan Imputation Server v1.1.2",
      "canceld": false,
      "complete": true,
      "currentTime": 1542210080745,
      "endTime": 0,
      "finishedOn": 0,
      "id": "job-20181106-152000-389",
      "name": "job-20181106-152000-389",
      "positionInQueue": -1,
      "priority": 0,
      "progress": -1,
      "setupEndTime": 0,
      "setupRunning": false,
      "setupStartTime": 0,
      "startTime": 0,
      "state": -1,
      "submittedOn": 1541514000433,
      "user": {
        "fullName": "Lukas Forer",
        "id": 1,
        "mail": "ttt@dsad.com",
        "roles": ["admin"],
        "username": "admin"
      },
      "workspaceSize": "366 bytes"
    }, {
      "app": null,
      "application": "Sleep 1.0",
      "canceld": false,
      "complete": true,
      "currentTime": 1542205817069,
      "endTime": 0,
      "finishedOn": 0,
      "id": "job-20181114-152921-374",
      "name": "job-20181114-152921-374",
      "positionInQueue": -1,
      "priority": 0,
      "progress": -1,
      "setupEndTime": 1542205761400,
      "setupRunning": false,
      "setupStartTime": 1542205761398,
      "startTime": 0,
      "state": 1,
      "submittedOn": 1542205761375,
      "user": {
        "fullName": "Lukas Forer",
        "id": 1,
        "mail": "ttt@dsad.com",
        "roles": ["admin"],
        "username": "admin"
      },
      "workspaceSize": "366 bytes"
    }, {
      "app": null,
      "application": "Sleep 1.0",
      "canceld": false,
      "complete": true,
      "currentTime": 1542205817069,
      "endTime": 1542205798236,
      "finishedOn": 1542205803206,
      "id": "job-20181114-152913-403",
      "name": "job-20181114-152913-403",
      "positionInQueue": -1,
      "priority": 0,
      "progress": -1,
      "setupEndTime": 1542205753432,
      "setupRunning": false,
      "setupStartTime": 1542205753430,
      "startTime": 1542205768231,
      "state": 4,
      "submittedOn": 1542205753403,
      "user": {
        "fullName": "Lukas Forer",
        "id": 1,
        "mail": "ttt@dsad.com",
        "roles": ["admin"],
        "username": "admin"
      },
      "workspaceSize": "366 bytes"
    }, {
      "application": "Michigan Imputation Server v1.1.2",
      "applicationId": "imputationserver",
      "canceld": false,
      "complete": false,
      "currentTime": 1541584969778,
      "deletedOn": -1,
      "endTime": 0,
      "finishedOn": 0,
      "id": "job-20181107-110238-723",
      "logs": "logs/job-20181107-110238-723",
      "name": "job-20181107-110238-723",
      "outputParams": [],
      "positionInQueue": 0,
      "priority": 2,
      "progress": -1,
      "running": true,
      "setupEndTime": 1541584962768,
      "setupRunning": false,
      "setupStartTime": 1541584958843,
      "startTime": 1541584967149,
      "state": 2,
      "steps": [],
      "submittedOn": 1541584958763,
      "user": {
        "fullName": "Lukas Forer",
        "id": 1,
        "mail": "ttt@dsad.com",
        "roles": ["admin"],
        "username": "admin"
      },
      "workspaceSize": "366 bytes"
    }, {
      "application": "Michigan Imputation Server v1.1.2",
      "applicationId": "imputationserver",
      "canceld": false,
      "complete": false,
      "currentTime": 1541584969778,
      "deletedOn": -1,
      "endTime": 0,
      "finishedOn": 0,
      "id": "job-20181107-110238-723",
      "logs": "logs/job-20181107-110238-723",
      "name": "job-20181107-110238-723",
      "outputParams": [],
      "positionInQueue": 0,
      "priority": 2,
      "progress": -1,
      "running": true,
      "setupEndTime": 1541584962768,
      "setupRunning": false,
      "setupStartTime": 1541584958843,
      "startTime": 1541584967149,
      "state": 3,
      "steps": [],
      "submittedOn": 1541584958763,
      "user": {
        "fullName": "Lukas Forer",
        "id": 1,
        "mail": "ttt@dsad.com",
        "roles": ["admin"],
        "username": "admin"
      },
      "workspaceSize": "366 bytes"
    }, {
      "application": "Michigan Imputation Server v1.1.2",
      "applicationId": "imputationserver",
      "canceld": false,
      "complete": false,
      "currentTime": 1541584969778,
      "deletedOn": -1,
      "endTime": 0,
      "finishedOn": 0,
      "id": "job-20181107-110238-723",
      "logs": "logs/job-20181107-110238-723",
      "name": "job-20181107-110238-723",
      "outputParams": [],
      "positionInQueue": 0,
      "priority": 2,
      "progress": -1,
      "running": true,
      "setupEndTime": 1541584962768,
      "setupRunning": true,
      "setupStartTime": 1541584958843,
      "startTime": 1541584967149,
      "state": 1,
      "steps": [],
      "submittedOn": 1541584958763,
      "user": {
        "fullName": "Lukas Forer",
        "id": 1,
        "mail": "ttt@dsad.com",
        "roles": ["admin"],
        "username": "admin"
      },
      "workspaceSize": "366 bytes"
    }, {
      "app": null,
      "application": "Michigan Imputation Server v1.1.2",
      "canceld": false,
      "complete": true,
      "currentTime": 1542210080744,
      "endTime": 1541585226624,
      "finishedOn": 1541585227137,
      "id": "job-20181107-110238-723",
      "name": "job-20181107-110238-723",
      "positionInQueue": -1,
      "priority": 0,
      "progress": -1,
      "setupEndTime": 1541584962768,
      "setupRunning": false,
      "setupStartTime": 1541584958843,
      "startTime": 1541584967149,
      "state": 7,
      "submittedOn": 1541584958763,
      "user": {
        "fullName": "Lukas Forer",
        "id": 1,
        "mail": "ttt@dsad.com",
        "roles": ["admin"],
        "username": "admin"
      },
      "workspaceSize": "366 bytes"
    },
    {
      "app": null,
      "application": "Allele Frequencies from VCF 1.0",
      "applicationId": "genepi-cloudgene-examples-vcf-tools",
      "canceld": false,
      "complete": true,
      "currentTime": 1542293660356,
      "deletedOn": 1519384252704,
      "endTime": 1519142181205,
      "finishedOn": 1519142186157,
      "id": "job-20180220-165617-683",
      "name": "job-20180220-165617-683",
      "positionInQueue": -1,
      "priority": 0,
      "progress": -1,
      "running": false,
      "setupEndTime": 1519142177873,
      "setupRunning": false,
      "setupStartTime": 1519142177871,
      "startTime": 1519142181196,
      "state": 9,
      "submittedOn": 1519142177851,
      "user": {
        "fullName": "Lukas Forer",
        "id": 1,
        "mail": "ttt@dsad.com",
        "roles": ["admin"],
        "username": "admin"
      },
      "workspaceSize": "1 KB"
    }
  ]
};

var list = new List();
list.attr('count', jobs1.count);
list.attr('success', jobs1.success);
list.attr('failed', jobs1.failed);
list.attr('pending', jobs1.pending);
list.attr('waiting', jobs1.waiting);
list.attr('running', jobs1.running);
list.attr('canceld', jobs1.canceld);
for (var i = 0; i < jobs1.data.length; i++) {
  var job = new JobDetails(jobs1.data[i]);
  job.syncTime();
  list.push(job);
}

$("#test-container").append(template({
  jobs: list
}));
$("#test-container").append("<br><br><br>");
