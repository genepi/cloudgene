// Job Details Page Controller

JobDetailsPage = can.Control({

  // load details from database and rendering

  "init": function(element, options) {
    var that = this;
    this.active = true;

    JobDetails.findOne({
        id: options.jobId
      }, function(job) {

        that.element.html(can.view('views/job-details.ejs', {
          job: job,
          results: options.results,
          admin: options.admin
        }));
        that.job = job;
        that.refresh();

      }, function(response) {
        new ErrorPage(that.element, response);
      }

    );
  },

  // delete job

  '#delete-btn click': function(el, ev) {
    var that = this;

    bootbox.confirm("Are you sure you want to delete <b>" + that.job.attr('id') + "</b>?", function(result) {
      if (result) {

        $("a[data-handler='1']").button('loading');
        $("a[data-handler='0']").hide('hide');

        that.job.destroy(function() {
          // go to jobs page
          bootbox.hideAll();
          window.location.hash = "!pages/jobs";
        }, function(response) {
          new ErrorPage(that.element, response);
        });

        return false;

      }
    });

  },

  // cancel job

  '#cancel-btn click': function(el, ev) {
    var that = this;

    bootbox.confirm("Are you sure you want to cancel <b>" + that.job.attr('id') + "</b>?", function(result) {
      if (result) {

        $("a[data-handler='1']").button('loading');
        $("a[data-handler='0']").hide('hide');

        var operation = new JobOperation();
        operation.attr('id', that.job.attr('id'));
        operation.attr('action', 'cancel');
        operation.save(function() {
          bootbox.hideAll();
          that.refresh();
        }, function(response) {
          new ErrorPage(that.element, response);
        });

        return false;

      }
    });

  },

  '#restart-btn click': function(el, ev) {
    var that = this;

    bootbox.confirm("Are you sure you want to restart <b>" + that.job.attr('id') + "</b>?", function(result) {
      if (result) {

        $("a[data-handler='1']").button('loading');
        $("a[data-handler='0']").hide('hide');

        var operation = new JobOperation();
        operation.attr('id', that.job.attr('id'));
        operation.attr('action', 'restart');
        operation.save(function() {
          bootbox.hideAll();
          if (that.options.admin) {
            window.location.hash = "!pages/admin-jobs";
          } else {
            window.location.hash = "!pages/jobs";
          }
        }, function(response) {
          new ErrorPage(that.element, response);
        });

        return false;

      }
    });
  },

  '.share-file-btn click': function(e) {
    output = e.closest('tr').data('output');

    bootbox.alert(can.view('/views/share-file.ejs', {
      hostname: location.protocol + '//' + location.host,
      output: output
    }), function() {

    });
  },

  '.share-folder-btn click': function(e) {
    files = e.closest('tr').data('files');

    bootbox.alert(can.view('/views/share-folder.ejs', {
      hostname: location.protocol + '//' + location.host,
      files: files
    }), function() {

    });
  },

  // refresh if job is running

  refresh: function() {
    var that = this;
    Job.findOne({
      id: that.job.id
    }, function(currentJob) {
      that.job.attr('state', currentJob.attr('state'));
      that.job.attr('startTime', currentJob.attr('startTime'));
      if (currentJob.attr('startTime') > 0 && currentJob.attr('endTime') === 0) {
        //running
        that.job.attr('endTime', currentJob.attr('currentTime'));
      } else {
        that.job.attr('endTime', currentJob.attr('endTime'));
      }
      that.job.attr('setupStartTime', currentJob.attr('setupStartTime'));
      if (currentJob.attr('setupStartTime') > 0 && currentJob.attr('setupEndTime') === 0) {
        //running
        that.job.attr('setupEndTime', currentJob.attr('currentTime'));
      } else {
        that.job.attr('setupEndTime', currentJob.attr('setupEndTime'));
      }
      that.job.attr('steps', currentJob.attr('steps'));
      that.job.attr('positionInQueue', currentJob.attr('positionInQueue'));

      // needs refresh
      if (JobDetailsPage.needsUpdate(currentJob) && that.active) {
        setTimeout(function() {
          that.refresh();
        }, 5000);
      } else {
        // updates details (results, startTime, endTime, ...)
        JobDetails.findOne({
          id: that.job.id
        }, function(job) {

          if (this.active) {

            that.element.html(can.view('views/job-details.ejs', {
              job: job
            }));
          }

        }, function(response) {
          new ErrorPage(that.element, response);

        });

      }

    });

  },

  destroy: function() {
    this.active = false;
    can.Control.prototype.destroy.call(this);
  }

});

JobDetailsPage.needsUpdate = function(job) {
  return job.attr("state") == 1 || job.attr("state") == 2 || job.attr("state") == 3;
};
