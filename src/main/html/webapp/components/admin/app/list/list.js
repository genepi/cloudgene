import can from 'can/legacy';
import $ from 'jquery';
import bootbox from 'bootbox';

import Application from 'models/application';

import template from './list.ejs';
import templateInstallGithub from './install-github/install-github.ejs';
import templateInstallUrl from './install-url/install-url.ejs';


export default can.Control({

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

    bootbox.confirm(
      can.view(templateInstallUrl()),
      function(result) {
        if (result) {
          var id = $('#id').val();
          var url = $('#url').val();
          var app = new Application();
          app.attr('url', url);
          app.attr('name', id);

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
        }
      });
  },

  '#install-app-github-btn click': function(el, ev) {

    bootbox.confirm(
      can.view(templateInstallGithub()),
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
                location.reload();
              });

            }, function(data) {
              waitingDialog.modal('hide');
              bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
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

    var application = el.closest('.card').data('application');

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

          }, function(data) {
            waitingDialog.modal('hide');
            bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
          });
        });
        waitingDialog.modal('show');

      }
    });
  },

  '.delete-app-btn click': function(el, ev) {

    var application = el.closest('.card').data('application');

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

          }, function(data) {
            waitingDialog.modal('hide');
            bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
          });

        });

        waitingDialog.modal('show');
      }
    });

  },

  '.edit-permission-btn click': function(el, ev) {

    var application = el.closest('.card').data('application');
    var permission = application.attr('permission');

    bootbox.confirm('<p>' + application.attr('id') + '</p><h4>Permissions' +
      '</h4><p></p><form><input class="form-control" id="message" name="message" value="' + permission + '"></input></form>',
      function(result) {
        if (result) {
          var text = $('#message').val();
          application.attr('permission', text);
          application.save();
        }
      });


  },

  '.reinstall-btn click': function(el, ev) {

    var application = el.closest('.card').data('application');

    bootbox.confirm('<h4>' + application.attr('id') + '</h4><p>Force reinstallation of application? <br>All metafile in HDFS are deleted and reimported on next job run.</p>',
      function(result) {
        if (result) {
          application.attr('reinstall', 'true');
          application.save();
        }
      });


  },

  '.view-source-btn click': function(el, ev) {

    var application = el.closest('.card').data('application');

    var env = '';
    for (var property in application.attr('environment').attr()) {
      env += property + '=' + application.attr('environment').attr(property) + '\n';
    }

    bootbox.alert('<h5>File</h5><p>' + application.attr('filename') + '</p>' + '<h5>Environment Variables</h5><p><pre><code>' + env + '</pre></code></p>' + '<h5>Source</h5><p><pre><code>' + application.attr('source') + '</code></pre></p>');
  }
});
