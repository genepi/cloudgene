import $ from 'jquery';
import 'popper.js';
import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
import '@fortawesome/fontawesome-free/css/all.css';
import 'components/core/layout/layout.css';

import 'helpers/helpers';

import template from './detail.stache';
import JobDetails from 'models/job-details';

var jobs = [{
    name: "Header Success",
    data: {
      "app": null,
      "application": "Michigan Imputation Server v1.1.2",
      "applicationId": "imputationserver",
      "canceld": false,
      "complete": true,
      "currentTime": 1541583821970,
      "deletedOn": -1,
      "endTime": 1541581332623,
      "finishedOn": 1541581337056,
      "id": "job-20181107-095757-370",
      "logs": "logs/job-20181107-095757-370",
      "name": "job-20181107-095757-370",
      "outputParams": [],
      "positionInQueue": -1,
      "priority": 0,
      "progress": -1,
      "running": false,
      "setupEndTime": 1541581081459,
      "setupRunning": false,
      "setupStartTime": 1541581077575,
      "startTime": 1541581082058,
      "state": 4,
      "steps": [],
      "submittedOn": 1541581077428,
      "workspaceSize": "",
      "username": "admin"
    }
  }, {
    name: "Header Pending",
    data: {
      "app": null,
      "application": "Michigan Imputation Server v1.1.2",
      "applicationId": "imputationserver",
      "canceld": false,
      "complete": true,
      "currentTime": 1541584817249,
      "deletedOn": 0,
      "endTime": 0,
      "finishedOn": 0,
      "id": "job-20181106-152000-389",
      "logs": "logs/job-20181106-152000-389",
      "name": "job-20181106-152000-389",
      "outputParams": [],
      "positionInQueue": -1,
      "priority": 0,
      "progress": -1,
      "running": false,
      "setupEndTime": 0,
      "setupRunning": false,
      "setupStartTime": 0,
      "startTime": 0,
      "state": -1,
      "steps": [],
      "submittedOn": 1541514000433,
      "workspaceSize": "",
      "username": "admin"
    }
  }, {
    name: 'Header Canceld',
    data: {
      "app": null,
      "application": "Michigan Imputation Server v1.1.2",
      "applicationId": "imputationserver",
      "canceld": false,
      "complete": true,
      "currentTime": 1541584821232,
      "deletedOn": -1,
      "endTime": 1541515066255,
      "finishedOn": 1541515067098,
      "id": "job-20181106-153600-718",
      "logs": "logs/job-20181106-153600-718",
      "name": "job-20181106-153600-718",
      "outputParams": [],
      "positionInQueue": -1,
      "priority": 0,
      "progress": -1,
      "running": false,
      "setupEndTime": 1541514964939,
      "setupRunning": false,
      "setupStartTime": 1541514960882,
      "startTime": 1541514967104,
      "state": 6,
      "steps": [],
      "submittedOn": 1541514960772,
      "workspaceSize": "",
      "username": "admin"
    }
  }, {
    name: 'Header Failed',
    data: {
      "app": null,
      "application": "Michigan Imputation Server v1.1.2",
      "applicationId": "imputationserver",
      "canceld": false,
      "complete": true,
      "currentTime": 1541584821232,
      "deletedOn": -1,
      "endTime": 1541515066255,
      "finishedOn": 1541515067098,
      "id": "job-20181106-153600-718",
      "logs": "logs/job-20181106-153600-718",
      "name": "job-20181106-153600-718",
      "outputParams": [],
      "positionInQueue": -1,
      "priority": 0,
      "progress": -1,
      "running": false,
      "setupEndTime": 1541514964939,
      "setupRunning": false,
      "setupStartTime": 1541514960882,
      "startTime": 1541514967104,
      "state": 5,
      "steps": [],
      "submittedOn": 1541514960772,
      "workspaceSize": "",
      "username": "admin"
    }
  }, {
    name: 'Header Running',
    data: {
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
      "workspaceSize": "",
      "username": "admin"
    }
  }, {
    name: 'Header Running Export',
    data: {
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
      "workspaceSize": "",
      "username": "admin"
    }
  },
  {
    name: 'Header Running Setup',
    data: {
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
      "workspaceSize": "",
      "username": "admin"
    }
  },
  {
    name: 'Header Waiting Queue',
    data: {
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
      "positionInQueue": 5,
      "priority": 2,
      "progress": -1,
      "running": false,
      "setupEndTime": 1541584962768,
      "setupRunning": false,
      "setupStartTime": 1541584958843,
      "startTime": 1541584967149,
      "state": 1,
      "steps": [],
      "submittedOn": 1541584958763,
      "workspaceSize": "",
      "username": "admin"
    }
  },
  {
    name: 'Header Retired',
    data: {
      "app": null,
      "application": "Michigan Imputation Server v1.1.2",
      "applicationId": "imputationserver",
      "canceld": false,
      "complete": true,
      "currentTime": 1541585911401,
      "deletedOn": -1,
      "endTime": 1541585226624,
      "finishedOn": 1541585227137,
      "id": "job-20181107-110238-723",
      "logs": "logs/job-20181107-110238-723",
      "name": "job-20181107-110238-723",
      "outputParams": [],
      "positionInQueue": -1,
      "priority": 0,
      "progress": -1,
      "running": false,
      "setupEndTime": 1541584962768,
      "setupRunning": false,
      "setupStartTime": 1541584958843,
      "startTime": 1541584967149,
      "state": 7,
      "steps": [],
      "submittedOn": 1541584958763,
      "workspaceSize": "",
      "username": "admin"
    }
  }

];


for (var i = 0; i < jobs.length; i++) {
  var job = new JobDetails(jobs[i].data);
  job.syncTime();
  $("#test-container").append("<h3>Test: " + jobs[i].name + "</h3>");
  $("#test-container").append(template({
    job: job,
    tab: '',
    admin: 'false'
  }));
  $("#test-container").append("<br><br><br>");
}
