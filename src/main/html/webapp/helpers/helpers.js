import can from 'can/legacy';
import dateFormat from 'dateformat';
import domData from 'can-util/dom/data/data';


can.EJS.Helpers.prototype.can = {};
can.EJS.Helpers.prototype.can.data = function(el, key, value){
  domData.set.call(el, key, value);
}

can.EJS.Helpers.prototype.prettyState = function(state) {

  if (this.state == 1) {
    return 'Waiting';
  } else if (this.state == 2) {
    return 'Running';
  } else if (this.state == 3) {
    return 'Exporting Data';
  } else if (this.state == 4) {
    return 'Complete';
  } else if (this.state == 5) {
    return 'Error';
  } else if (this.state == 6) {
    return 'Canceled';
  } else {
    return 'Error';
  }

};

can.EJS.Helpers.prototype.prettyTime = function(start, end, current) {

  if (start === 0 && end === 0) {
    return '-';
  }

  var executionTime = 0;
  if (start > 0 && end === 0) {
    executionTime = current - start;
  } else {
    executionTime = end - start;
  }

  if (executionTime <= 0) {

    return '-';

  } else {

    var h = (Math.floor((executionTime / 1000) / 60 / 60));
    var m = ((Math.floor((executionTime / 1000) / 60)) % 60);

    return (h > 0 ? h + ' h ' : '') + (m > 0 ? m + ' min ' : '') +
      ((Math.floor(executionTime / 1000)) % 60) + ' sec';

  }

};

can.EJS.Helpers.prototype.prettyDate = function(unixTimestamp) {

  if (unixTimestamp > 0) {
    var dt = new Date(unixTimestamp);
    return dateFormat(dt, "default");
  } else {
    return '-';
  }

};

can.EJS.Helpers.prototype.getClassByJob = function(job) {
  if (job.attr('state') == '-1') {
    return 'job-dark';
  }
  if (job.attr('state') == '1') {
    if (job.attr('setupRunning')) {
      return 'job-secondary';
    } else {
      return 'job-secondary';
    }
  }
  if (job.attr('state') == '2') {
    return 'job-primary';
  }
  if (job.attr('state') == '3') {
    return 'job-primary';
  }
  if (job.attr('state') == '4' || job.attr('state') == '8') {
    return 'job-success';
  }
  if (job.attr('state') == '5') {
    return 'job-danger';
  }
  if (job.attr('state') == '6') {
    return 'job-danger';
  }
  if (job.attr('state') == '7') {
    return 'job-dark';
  }
};

String.prototype.endsWith = function(s) {
  return this.length >= s.length && this.substr(this.length - s.length) == s;
};
