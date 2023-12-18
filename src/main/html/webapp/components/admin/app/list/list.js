import Control from 'can-control';
import domData from 'can-util/dom/data/data';
import canMap from 'can-map';
import canRoute from 'can-route';

import 'helpers/helpers';
import $ from 'jquery';
import bootbox from 'bootbox';
import showErrorDialog from 'helpers/error-dialog';

import Application from 'models/application';
import Group from 'models/group';

import template from './list.stache';
import templateInstallGithub from './install-github/install-github.stache';
import templateInstallUrl from './install-url/install-url.stache';
import templatePermission from './permission/permission.stache';
import templateSettings from './settings/settings.stache';

export default Control.extend({

  "init": function(element, options) {
    var that = this;

    Application.findAll({}, function(applications) {
      that.options.installedApplications = applications;
      $(element).html(template({
        applications: applications
      }));
      $(element).fadeIn();

    });

  },

  '#install-app-url-btn click': function(el, ev) {

    bootbox.confirm(templateInstallUrl(),
      function(result) {
        if (result) {
          var url = $('#url').val();
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
        }
      });
  },

  '#install-app-github-btn click': function(el, ev) {

    bootbox.confirm(templateInstallGithub(),
      function(result) {
        if (result) {

          var url = 'github://' + $('#url').val();
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
        }
      });
  },

  '#reload-apps-btn click': function(el, ev) {
    var element = this.element;

    Application.findAll({
      reload: 'true'
    }, function(applications) {

      $(element).html(template({
        applications: applications
      }));
      $("#content").fadeIn();

    });
  },

  '.enable-disable-btn click': function(el, ev) {
    var card = $(el).closest('.card');
    var application = domData.get.call(card[0], 'application');

    var enabled = !application.attr('enabled')
    bootbox.confirm("Are you sure you want to " + (enabled ? "enable" : "disable") + " application <b>" + application.attr('id') + "</b>?", function(result) {
      if (result) {
        application.attr('enabled', enabled);

        var waitingDialog = bootbox.dialog({
          message: (enabled ? '<h4>Enable application</h4>' : '<h4>Disable application</h4>') +
            '<p>Please wait while the application is configured.</p>' +
            '<div class="progress progress-striped active">' +
            '<div id="waiting-progress" class="bar" style="width: 100%;"></div>' +
            '</div>',
          show: false
        });
        waitingDialog.on('shown.bs.modal', function() {

          application.save(function(application) {
            waitingDialog.modal('hide');
            bootbox.alert('<h4>Congratulations</h4><p>The application has been successfully ' + (enabled ? 'enabled' : 'disabled') + '.</p>');

          }, function(response) {
            waitingDialog.modal('hide');
            showErrorDialog("Operation failed", response);
          });
        });
        waitingDialog.modal('show');

      }
    });
  },

  '.delete-app-btn click': function(el, ev) {

    var card = $(el).closest('.card');
    var application = domData.get.call(card[0], 'application');

    bootbox.confirm("Are you sure you want to delete <b>" + application.attr('id') + "</b>?", function(result) {
      if (result) {

        var waitingDialog = bootbox.dialog({
          message: '<h4>Uninstall application</h4>' +
            '<p>Please wait while the application is configured.</p>' +
            '<div class="progress progress-striped active">' +
            '<div id="waiting-progress" class="bar" style="width: 100%;"></div>' +
            '</div>',
          show: false
        });

        waitingDialog.on('shown.bs.modal', function() {

          application.destroy(function(application) {
            waitingDialog.modal('hide');
            bootbox.alert('<h4>Congratulations</h4><p>The application has been successfully removed.</p>');

          }, function(response) {
            waitingDialog.modal('hide');
            showErrorDialog("Operation failed", response);
          });

        });

        waitingDialog.modal('show');
      }
    });

  },

  '.edit-settings-btn click': function(el, ev) {

    var card = $(el).closest('.card');
    var application = domData.get.call(card[0], 'application');


    bootbox.confirm(templateSettings({
        application: application
      }),
      function(result) {
        if (result) {
          var nextflowProfile = $('#nextflow-profile').val();
          var nextflowConfig = $('#nextflow-config').val();
          var nextflowWork = $('#nextflow-work').val();

          application.attr('config').attr('nextflow.profile', nextflowProfile);
          application.attr('config').attr('nextflow.config', nextflowConfig);
          application.attr('config').attr('nextflow.work', nextflowWork);
          application.save(function(data) {},
            function(response) {
              showErrorDialog("Operation failed", response);
            });
        }
      }).find("div.modal-dialog").css({ "width": "80%" });

  },


  '.edit-permission-btn click': function(el, ev) {

    var card = $(el).closest('.card');
    var application = domData.get.call(card[0], 'application');

    Group.findAll({},
      function(groups) {

        var roles = application.attr('permission').split(',');

        var options = '';
        groups.forEach(function(group, index) {
          if ($.inArray(group.attr('name'), roles) >= 0) {
            options = options + '<label class="checkbox"><input type="checkbox" name="role-select" value="' + group.attr('name') + '" checked />';
            //options = options + '<option selected>' + group.attr('name') + '</option>';
          } else {
            //options = options + '<option>' + group.attr('name') + '</option>';
            options = options + '<label class="checkbox"><input type="checkbox" name="role-select" value="' + group.attr('name') + '" />';
          }
          options = options + ' <b>' + group.attr('name') + '</b></label><br>';
        });

        bootbox.confirm(
          '<h4>Edit permission of ' + application.attr('name') + '</h4><hr><form id="role-form">' + options + '</form>',
          function(result) {
            if (result) {

              var boxes = $('#role-form input:checkbox');
              var checked = [];
              for (var i = 0; boxes[i]; ++i) {
                if (boxes[i].checked) {
                  checked.push(boxes[i].value);
                }
              }

              var text = checked.join(',');
              application.attr('permission', text);
              application.save(function(data) {},
                function(response) {
                  showErrorDialog("Operation failed", response);
                });

            }
          }
        );

      },
      function(response) {
        new ErrorPage(element, response);
      });

  },

  '.reinstall-btn click': function(el, ev) {

    var card = $(el).closest('.card');
    var application = domData.get.call(card[0], 'application');
    bootbox.confirm('<h4>' + application.attr('id') + '</h4><p>Force reinstallation of application? <br>All metafiles in HDFS are deleted and reimported on next job run.</p>',
      function(result) {
        if (result) {
          application.attr('reinstall', 'true');
          application.save(function(data) {},
            function(response) {
              showErrorDialog("Operation failed", response);
            });
        }
      });


  },

  '.view-source-btn click': function(el, ev) {

    var card = $(el).closest('.card');
    var application = domData.get.call(card[0], 'application');
    var env = '';
    for (var property in application.attr('environment').attr()) {
      env += property + '=' + application.attr('environment').attr(property) + '\n';
    }

    bootbox.alert('<h5>File</h5><p>' + application.attr('filename') + '</p><h5>Status</h5><p>' + application.attr('state') + '</p>' + '<h5>Environment Variables</h5><p><pre><code>' + env + '</pre></code></p>' + '<h5>Source</h5><p><pre><code>' + application.attr('source') + '</code></pre></p>');
  }
});
