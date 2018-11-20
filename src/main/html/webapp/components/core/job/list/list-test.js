import $ from 'jquery';
import 'popper.js';
import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
import '@fortawesome/fontawesome-free/css/all.css';
import 'components/core/layout/layout.css';
import List from 'can-list';

import 'helpers/helpers';

import template from './list.stache';
import JobDetails from 'models/job-details';

var jobs = {
  "count": 131,
  "page": 1,
  "next": 2,
  "pages": [1, 2, 3, 4],
  "data": [{
    "app": null,
    "application": "Michigan Imputation Server v1.1.2",
    "canceld": false,
    "complete": true,
    "currentTime": 1542210080744,
    "endTime": 1542184770689,
    "finishedOn": 1542184772781,
    "id": "job-20181114-093840-519",
    "name": "job-20181114-093840-519",
    "positionInQueue": -1,
    "priority": 0,
    "progress": -1,
    "setupEndTime": 1542184722641,
    "setupRunning": false,
    "setupStartTime": 1542184720653,
    "startTime": 1542184722793,
    "state": 5,
    "submittedOn": 1542184720522,
    "workspaceSize": ""
  },{
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
    "workspaceSize": ""
  },{
    "app": null,
    "application": "Michigan Imputation Server v1.1.2",
    "canceld": false,
    "complete": true,
    "currentTime": 1542210080745,
    "endTime": 1541515066255,
    "finishedOn": 1541515067098,
    "id": "job-20181106-153600-718",
    "name": "job-20181106-153600-718",
    "positionInQueue": -1,
    "priority": 0,
    "progress": -1,
    "setupEndTime": 1541514964939,
    "setupRunning": false,
    "setupStartTime": 1541514960882,
    "startTime": 1541514967104,
    "state": 6,
    "submittedOn": 1541514960772,
    "workspaceSize": ""
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
    "workspaceSize": ""
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
    "workspaceSize": ""
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
    "workspaceSize": ""
  },{
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
  },{
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
  },{
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
  }]
};


var list = new List();
list.attr('count', jobs.count);
list.attr('page', jobs.page);
list.attr('pages', jobs.pages);
list.attr('next', jobs.next);
for (var i = 0; i < jobs.data.length; i++) {
  var job = new JobDetails(jobs.data[i]);
  job.syncTime();
  list.push(job);
}

$("#test-container").append(template({
  jobs: list
}));
$("#test-container").append("<br><br><br>");
