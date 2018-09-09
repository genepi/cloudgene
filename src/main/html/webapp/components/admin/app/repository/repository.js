import can from 'can';
import $ from 'jquery';
import bootbox from 'bootbox';

import Application from 'models/application';
import CloudgeneApplication from 'models/cloudgene-application';

import template from './repository.ejs';


export default can.Control({

  "init": function(element, options) {

    var that = this;

    Application.findAll({}, function(applications) {
      that.options.installedApplications = applications;

      CloudgeneApplication.findAll({}, function(applications) {
        var installedId = [];
        can.each(that.options.installedApplications, function(value, index) {
          installedId.push(value.attr('id'));
        });

        that.element.html(template({
          applications: applications,
          installedId: installedId
        }));
      });
      $("#content").fadeIn();

    });
  },

  '.install-app-btn click': function(el, ev) {

    var app = new Application();
    app.attr('name', el.data('app-id'));
    app.attr('url', el.data('app-url'));

    var waitingDialog = bootbox.dialog({
      message: '<h4>Install application</h4>' +
        '<p>Please wait while the application is configured.</p>' +
        '<div class="progress progress-striped active">' +
        '<div id="waiting-progress" class="bar" style="width: 100%;"></div>' +
        '</div>',
      show: false
    });

    waitingDialog.on('shown.bs.modal', function() {

      app.save(function(application) {
        waitingDialog.modal('hide');
        bootbox.alert('<h4>Congratulations</h4><p>The application installation was successful.</p>', function() {
          location.reload();
        });

      }, function(data) {
        waitingDialog.modal('hide');
        bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
      });
    });
    waitingDialog.modal('show');
  },

});
