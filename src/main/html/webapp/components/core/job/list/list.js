import can from 'can';
import $ from 'jquery';
import bootbox from 'bootbox';

import ErrorPage from 'helpers/error-page';
import Job from 'models/job';
import JobOperation from 'models/job-operation';

import template from './list.ejs';


export default can.Control({

  "init": function(element, options) {
    this.options.refreshers = [];

    var that = this;
    Job.findAll({
      page: options.page2
    }, function(jobs) {
      $.each(jobs, function(key, job) {
        $.each(jobs, function(key, job) {
          if (job.attr('startTime') > 0 && job.attr('endTime') === 0) {
            //running
            job.attr('endTime', job.attr('currentTime'));
          } else {
            job.attr('endTime', job.attr('endTime'));
          }
          if (job.attr('setupStartTime') > 0 && job.attr('setupEndTime') === 0) {
            //running
            job.attr('setupEndTime', job.attr('currentTime'));
          } else {
            job.attr('setupEndTime', job.attr('setupEndTime'));
          }
        });
        if (JobRefresher.needsUpdate(job)) {
          var refresher = new JobRefresher();
          refresher.setJob(job);
          that.options.refreshers.push(refresher);
        }
      });
      that.element.html(template({
        jobs: jobs,
        page: options.page2,
        total: jobs.attr('count'),
        perPage: 25,
      }));

      that.element.fadeIn();
    }, function(response) {
      new ErrorPage(that.element, response);
    });

  },

  '.delete-btn click': function(el, ev) {

    var job = el.closest('.card').data('job');

    bootbox.confirm("Are you sure you want to delete <b>" + job.attr('id') + "</b>?", function(result) {
      if (result) {

        $("a[data-handler='1']").button('loading');
        $("a[data-handler='0']").hide('hide');

        var that = this;

        job.destroy(function() {
          // go to jobs page
          bootbox.hideAll();
        }, function(response) {
          new ErrorPage(that.element, response);
        });

        return false;

      }

    });

  },

  '.cancel-btn click': function(el, ev) {
    var that = this;

    var job = el.closest('.card').data('job');

    bootbox.confirm("Are you sure you want to cancel <b>" + job.attr('id') + "</b>?", function(result) {
      if (result) {
        // cancel

        $("a[data-handler='1']").button('loading');
        $("a[data-handler='0']").hide('hide');

        var operation = new JobOperation();
        operation.attr('id', job.attr('id'));
        operation.attr('action', 'cancel');
        operation.save(function() {
          // go to jobs page
          bootbox.hideAll();
        }, function(response) {
          new ErrorPage(that.element, response);
        });

        return false;

      }
    });

  },

  destroy: function() {

    // stops all job refreshers!
    $.each(this.options.refreshers, function(key, refresher) {
      refresher.stop();
    });

    can.Control.prototype.destroy.call(this);
  }

});

var JobRefresher = can.Control({

  setJob: function(job) {
    this.job = job;
    this.active = true;
    this.refresh();
  },

  refresh: function() {
    var that = this;
    Job.findOne({
      id: that.job.id
    }, function(currentJob) {

      if (currentJob.attr('startTime') > 0 && currentJob.attr('endTime') === 0) {
        //running
        currentJob.attr('endTime', currentJob.attr('currentTime'));
      } else {
        currentJob.attr('endTime', currentJob.attr('endTime'));
      }
      if (currentJob.attr('setupStartTime') > 0 && currentJob.attr('setupEndTime') === 0) {
        //running
        currentJob.attr('setupEndTime', currentJob.attr('currentTime'));
      } else {
        currentJob.attr('setupEndTime', currentJob.attr('setupEndTime'));
      }

      if (JobRefresher.needsUpdate(currentJob) && that.active) {
        setTimeout(function() {
          that.refresh();
        }, 5000);
      }
    }, function(response) {
      new ErrorPage(that.element, response);
    });

  },

  stop: function() {
    this.active = false;
  }

});

JobRefresher.needsUpdate = function(job) {
  return job.attr("state") == 1 || job.attr("state") == 2 || job.attr("state") == 3;
};
