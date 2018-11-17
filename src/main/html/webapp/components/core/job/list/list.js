import Control from 'can-control';
import domData from 'can-util/dom/data/data';
import $ from 'jquery';
import bootbox from 'bootbox';

import 'helpers/helpers';
import ErrorPage from 'helpers/error-page';
import Job from 'models/job';
import JobOperation from 'models/job-operation';

import template from './list.stache';

export default Control.extend({

  "init": function(element, options) {

    this.options.refreshers = [];

    var that = this;
    Job.findAll({
      page: options.page
    }, function(jobs) {
      $.each(jobs, function(key, job) {
        $.each(jobs, function(key, job) {
          job.syncTime();
        });
        if (JobRefresher.needsUpdate(job)) {
          var refresher = new JobRefresher(element);
          refresher.setJob(job);
          that.options.refreshers.push(refresher);
        }
      });
      $(element).html(template({
        jobs: jobs
      }));
      $('[data-toggle="tooltip"]').tooltip()

      $(element).fadeIn();
    }, function(response) {
      new ErrorPage(element, response);
    });

  },

  '.delete-btn click': function(el, ev) {

    var card = $(el).closest('.card');
    var job = domData.get.call(card[0], 'job');

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
    var element = this.element;

    var card = $(el).closest('.card');
    var job = domData.get.call(card[0], 'job');

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
          new ErrorPage(element, response);
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

    Control.prototype.destroy.call(this);
  }

});

var JobRefresher = Control({

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

      currentJob.syncTime();

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
