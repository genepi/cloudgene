import can from 'can';
import $ from 'jquery';
import bootbox from 'bootbox';

import ErrorPage from 'helpers/error-page';
import Helper from 'helpers/helpers';
import JobAdminDetails from 'models/job-admin-details';
import JobOperation from 'models/job-operation';

import template from './list.ejs';
import templateTable from './table/table.ejs';


export default can.Control({

  "init": function(element, options) {
    element.html(template());
    element.fadeIn();

    this.loadJobs("#job-list-running-stq", "running-stq");
    this.loadJobs("#job-list-running-ltq", "running-ltq");
    this.loadJobs("#job-list-current", "current");


  },

  loadJobs: function(element, mySate) {
    var that = this;
    JobAdminDetails.findAll({
      state: mySate
    }, function(jobs) {
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
      $(element).html(templateTable(jobs));
    }, function(response) {
      new ErrorPage(that.element, response);
    });

  },

  '.delete-btn click': function(el, ev) {

    var job = el.closest('tr').data('job');

    bootbox.confirm("Are you sure you want to delete <b>" + job.attr('id') + "</b>?", function(result) {
      if (result) {
        job.destroy();
      }
    });

  },

  '.cancel-btn click': function(el, ev) {

    var job = el.closest('tr').data('job');

    var element = this.element;

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

  '.priority-btn click': function(el, ev) {
    var job = el.closest('tr').data('job');
    var that = this;
    $.get('api/v2/admin/jobs/' + job.attr('id') + '/priority').then(
      function(data) {
        bootbox.alert(data);
        that.init(that.element, that.options);
      },
      function(data) {
        bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
      });
  },

  '.archive-btn click': function(el, ev) {
    var job = el.closest('tr').data('job');
    var that = this;

    bootbox.confirm("Are you sure you want to archive <b>" + job.attr('id') + "</b> now? <b>All results will be deleted!</b>", function(result) {
      if (result) {
        $.get('api/v2/admin/jobs/' + job.attr('id') + '/archive').then(
          function(data) {
            bootbox.alert(data);
            that.init(that.element, that.options);
          },
          function(data) {
            bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
          });
      }
    });
  },

  '.reset-downloads-btn click': function(el, ev) {
    var job = el.closest('tr').data('job');
    $.get('api/v2/admin/jobs/' + job.attr('id') + '/reset').then(
      function(data) {
        bootbox.alert(data);
      },
      function(data) {
        bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
      });
  },

  '.retire-btn click': function(el, ev) {
    var job = el.closest('tr').data('job');
    var that = this;
    $.get('api/v2/admin/jobs/' + job.attr('id') + '/retire').then(
      function(data) {
        bootbox.alert(data);
        that.init(that.element, that.options);
      },
      function(data) {
        bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
      });
  },

  '.change-retire-date-btn click': function(el, ev) {
    var job = el.closest('tr').data('job');
    var that = this;
    $.get('api/v2/admin/jobs/' + job.attr('id') + '/change-retire/1').then(
      function(data) {
        bootbox.alert(data);
        that.init(that.element, that.options);
      },
      function(data) {
        bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
      });
  }

});
