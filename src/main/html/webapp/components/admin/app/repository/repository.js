import Control from 'can-control';
import $ from 'jquery';
import bootbox from 'bootbox';

import Application from 'models/application';
import CloudgeneApplication from 'models/cloudgene-application';

import template from './repository.ejs';


export default Control.extend({

  "init": function(element, options) {

    var that = this;

    Application.findAll({}, function(applications) {
      that.options.installedApplications = applications;

      CloudgeneApplication.findAll({}, function(applications) {
        var installedId = [];
        $.each(that.options.installedApplications, function(value, index) {
          installedId.push(value.attr('id'));
        });

        $(element).html(template({
          applications: applications,
          installedId: installedId
        }));
      });
      $("#content").fadeIn();

    });
  },

  '.install-app-btn click': function(el, ev) {

    var id = $(el).data('app-id');
    var url = $(el).data('app-url');

    var app = new Application();
    app.attr('name', id);
    app.attr('url', url);

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
