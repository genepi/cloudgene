import 'can-map-define';
import Control from 'can-control';
import domData from 'can-util/dom/data/data';
import $ from 'jquery';
import bootbox from 'bootbox';
import 'helpers/helpers';

import ErrorPage from 'helpers/error-page';
import showErrorDialog from 'helpers/error-dialog';
import 'helpers/helpers';
import JobAdminDetails from 'models/job-admin-details';
import JobOperation from 'models/job-operation';

import template from './table.stache';

export default Control.extend({

  "init": function(element, options) {
    JobAdminDetails.findAll({
      state: options.state
    }, function(jobs) {
      $.each(jobs, function(key, job) {
        job.syncTime();
      });
      $(element).html(template({
        jobs: jobs
      }));
    }, function(response) {
      new ErrorPage(element, response);
    });
  },

  '.delete-btn click': function(el, ev) {
    var tr = $(el).closest('tr');
    var job = domData.get.call(tr[0], 'job');

    bootbox.confirm("Are you sure you want to delete <b>" + job.attr('id') + "</b>?", function(result) {
      if (result) {

        var okButton = $("button[data-bb-handler='confirm']");
        okButton.prop('disabled', true);
        okButton.html('Please wait...');
        var cancelButton = $("button[data-bb-handler='cancel']");
        cancelButton.hide('hide');

        job.destroy(function() {
          // go to jobs page
          bootbox.hideAll();
          window.location.hash = "!pages/jobs";
        }, function(response) {
          bootbox.hideAll();
          showErrorDialog("Job could not be deleted", response);
        });

        return false;

      }
    });

  },

  '.cancel-btn click': function(el, ev) {

    var tr = $(el).closest('tr');
    var job = domData.get.call(tr[0], 'job');

    bootbox.confirm("Are you sure you want to cancel <b>" + job.attr('id') + "</b>?", function(result) {
      if (result) {

        var okButton = $("button[data-bb-handler='confirm']");
        okButton.prop('disabled', true);
        okButton.html('Please wait...');
        var cancelButton = $("button[data-bb-handler='cancel']");
        cancelButton.hide('hide');

        var operation = new JobOperation();
        operation.attr('id', job.attr('id'));
        operation.attr('action', 'cancel');
        operation.save(function() {
          bootbox.hideAll();
        }, function(response) {
          bootbox.hideAll();
          showErrorDialog("Job could not be canceld", response);
        });

        return false;
      }
    });

  },

  '.priority-btn click': function(el, ev) {

    var tr = $(el).closest('tr');
    var job = domData.get.call(tr[0], 'job');
    var that = this;
    $.get('api/v2/admin/jobs/' + job.attr('id') + '/priority').then(
      function(data) {
        bootbox.alert(data);
        that.init(that.element, that.options);
      },
      function(response) {
        showErrorDialog("Operation failed", response);
      });
  },

  '.archive-btn click': function(el, ev) {
    var tr = $(el).closest('tr');
    var job = domData.get.call(tr[0], 'job');
    var that = this;

    bootbox.confirm("Are you sure you want to archive <b>" + job.attr('id') + "</b> now? <b>All results will be deleted!</b>", function(result) {
      if (result) {
        $.get('api/v2/admin/jobs/' + job.attr('id') + '/archive').then(
          function(data) {
            bootbox.alert(data);
            that.init(that.element, that.options);
          },
          function(response) {
            showErrorDialog("Operation failed", response);
          });
      }
    });
  },

  '.reset-downloads-btn click': function(el, ev) {
    var tr = $(el).closest('tr');
    var job = domData.get.call(tr[0], 'job');
    $.get('api/v2/admin/jobs/' + job.attr('id') + '/reset').then(
      function(data) {
        bootbox.alert(data);
      },
      function(response) {
        showErrorDialog("Operation failed", response);
      });
  },

  '.unlimited-downloads-btn click': function(el, ev) {
    var tr = $(el).closest('tr');
    var job = domData.get.call(tr[0], 'job');
    $.get('api/v2/admin/jobs/' + job.attr('id') + '/reset?max=-1').then(
      function(data) {
        bootbox.alert(data);
      },
      function(response) {
        showErrorDialog("Operation failed", response);
      });
  },


  '.retire-btn click': function(el, ev) {
    var tr = $(el).closest('tr');
    var job = domData.get.call(tr[0], 'job');
    var that = this;
    $.get('api/v2/admin/jobs/' + job.attr('id') + '/retire').then(
      function(data) {
        bootbox.alert(data);
        that.init(that.element, that.options);
      },
      function(response) {
        showErrorDialog("Operation failed", response);
      });
  },

  '.change-retire-date-btn click': function(el, ev) {
    var tr = $(el).closest('tr');
    var job = domData.get.call(tr[0], 'job');
    var that = this;

    bootbox.confirm(
      '<h4>Retire Date</h4><p>Please enter the number of days:</p><form><input class="form-control" id="message" name="message" value="1">',
      function(result) {
        if (result) {
          var days = $('#message').val();
          $.get('api/v2/admin/jobs/' + job.attr('id') + '/change-retire/' + days).then(
            function(data) {
              bootbox.alert(data);
              that.init(that.element, that.options);
            },
            function(response) {
              showErrorDialog("Operation failed", response);
            });
        }
      }
    );
  }

});
