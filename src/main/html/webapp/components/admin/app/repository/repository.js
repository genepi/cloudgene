import Control from 'can-control';
import $ from 'jquery';
import bootbox from 'bootbox';
import showErrorDialog from 'helpers/error-dialog';
import canRoute from 'can-route';

import Application from 'models/application';
import CloudgeneApplication from 'models/cloudgene-application';

import template from './repository.stache';


export default Control.extend({

  "init": function(element, options) {

    Application.findAll({}, function(applications) {
      var installedApplications = applications;

      CloudgeneApplication.findAll({}, function(applications) {
        var installedId = [];
        $.each(installedApplications, function(index, application) {
          var tiles = application.attr('id').split("@");
          var installedApplication = {
            id: tiles[0],
            version: tiles[1]
          };
          installedId.push(installedApplication);
        });

        $.each(applications, function(index, application) {

          var installedApplication = installedId.filter(function(e) {
            return e.id === application.attr('id');
          });

          var installed = installedApplication.length > 0;
          application.attr('installed', installed);

          if (installed){
            var localVersion = installedApplication[0].version;
            application.attr('localVersion', localVersion);
          }


        });

        $(element).html(template({
          applications: applications,
          installedId: installedId
        }));
      });
      $(element).fadeIn();

    });
  },

  '.install-app-btn click': function(el, ev) {

    var url = $(el).data('app-url');

    var app = new Application();
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
          var router = canRoute.router;
          router.reload();
        });

      }, function(response) {
        waitingDialog.modal('hide');
        showErrorDialog("Operation failed", response);
      });
    });
    waitingDialog.modal('show');
  },

});
