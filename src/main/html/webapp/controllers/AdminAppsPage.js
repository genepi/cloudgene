// controller
AdminAppsPage = can.Control({

  "init": function(element, options) {
    var that = this;

    Application.findAll({}, function(applications) {
      that.options.installedApplications = applications;
      that.element.html(can.view('views/admin/apps.ejs', {
        applications: applications
      }));
      $("#content").fadeIn();

    });

  },

  '#install-app-url-btn click': function(el, ev) {
    bootbox.animate(false);
    bootbox.confirm(
      can.view('views/admin/apps.install.url.ejs'),
      function(result) {
        if (result) {
          var id = $('#id').val();
          var url = $('#url').val();
          app = new Application();
          app.attr('url', url);
          app.attr('name', id);

          bootbox.dialog('<h4>Install application</h4>' +
            '<p>Please wait while the application is configured.</p>' +
            '<div class="progress progress-striped active">' +
            '<div id="waiting-progress" class="bar" style="width: 100%;"></div>' +
            '</div>'
          );

          app.save(function(application) {
            bootbox.hideAll();
            bootbox.alert('<h4>Congratulations</h4><p>The application installation was successful.</p>', function() {
              location.reload();
            });

          }, function(data) {
            bootbox.hideAll();
            bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
          });

        }
      });

  },

  '#install-app-github-btn click': function(el, ev) {
    bootbox.animate(false);
    bootbox.confirm(
      can.view('views/admin/apps.install.github.ejs'),
      function(result) {
        if (result) {
          var url = 'github://' + $('#url').val();
          app = new Application();
          app.attr('url', url);

          bootbox.dialog('<h4>Install application</h4>' +
            '<p>Please wait while the application is configured.</p>' +
            '<div class="progress progress-striped active">' +
            '<div id="waiting-progress" class="bar" style="width: 100%;"></div>' +
            '</div>'
          );

          app.save(function(application) {
            bootbox.hideAll();
            bootbox.alert('<h4>Congratulations</h4><p>The application installation was successful.</p>', function() {
              location.reload();
            });

          }, function(data) {
            bootbox.hideAll();
            bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
          });

        }
      });

  },

  '#install-app-cloudgene-io-btn click': function(el, ev) {

    that = this;
    console.log(that.options.installedApplications);

    CloudgeneApplication.findAll({}, function(applications) {
        var installedId = [];
        can.each(that.options.installedApplications, function(value, index) {
          installedId.push(value.attr('id'));
        });

        console.log(installedId);

        var content = can.view('views/admin/apps.repository.ejs', {
          applications: applications,
          installedId: installedId

        });
        bootbox.animate(false);
        bootbox.confirm(content);
        new AdminAppsInstallerDialog('#application-repository', {});
      }, function(dadsad) {

        console.log(dadsad);
      }

    );
  },

  '#reload-apps-btn click': function(el, ev) {
    var that = this;

    Application.findAll({
      reload: 'true'
    }, function(applications) {

      that.element.html(can.view('views/admin/apps.ejs', {
        applications: applications
      }));
      $("#content").fadeIn();

    });
  },

  '.enabled-checkbox click': function(el, ev) {

    application = el.closest('tr').data('application');
    bootbox.animate(false);
    enabled = el.is(":checked");
    bootbox.confirm("Are you sure you want to " + (enabled ? "enable" : "disable") + " application <b>" + application.attr('id') + "</b>?", function(result) {
      if (result) {
        application.attr('enabled', enabled);

        bootbox.dialog((enabled ? '<h4>Install application</h4>' : '<h4>Uninstall application</h4>') +
          '<p>Please wait while the application is configured.</p>' +
          '<div class="progress progress-striped active">' +
          '<div id="waiting-progress" class="bar" style="width: 100%;"></div>' +
          '</div>'
        );

        application.save(function(application) {
          bootbox.hideAll();
          bootbox.alert(enabled ? '<h4>Congratulations</h4><p>The application installation was successful.</p>' : '<h4>Congratulations</h4><p>The application has been successfully removed.</p>');

        }, function(data) {
          bootbox.hideAll();
          bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
        });
      } else {
        //reset checkbox
        el.prop("checked", !enabled);
      }
    });
  },

  '.delete-app-btn click': function(el, ev) {

    application = el.closest('tr').data('application');
    bootbox.animate(false);
    bootbox.confirm("Are you sure you want to delete <b>" + application.attr('id') + "</b>?", function(result) {
      if (result) {

        bootbox.dialog('<h4>Uninstall application</h4>' +
          '<p>Please wait while the application is configured.</p>' +
          '<div class="progress progress-striped active">' +
          '<div id="waiting-progress" class="bar" style="width: 100%;"></div>' +
          '</div>'
        );

        application.destroy(function(application) {
          bootbox.hideAll();
          bootbox.alert('<h4>Congratulations</h4><p>The application has been successfully removed.</p>');

        }, function(data) {
          bootbox.hideAll();
          bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
        });
      }
    });

  },

  '.edit-permission-btn click': function(el, ev) {

    application = el.closest('tr').data('application');
    var permission = application.attr('permission');
    bootbox.animate(false);

    bootbox.confirm('<h4> Change permission of ' + application.attr('id') +
      '</h4><form><input class="field span2" id="message" name="message" value="' + permission + '"></input></form>',
      function(result) {
        if (result) {
          var text = $('#message').val();
          application.attr('permission', text);
          application.save();
        }
      });


  },

  '.reinstall-btn click': function(el, ev) {

    application = el.closest('tr').data('application');
    var permission = application.attr('permission');
    bootbox.animate(false);

    bootbox.confirm('<h4>' + application.attr('id') + '</h4><p>Force reinstallation of application? <br>All metafile in HDFS are deleted and reimported on next job run.</p>',
      function(result) {
        if (result) {
          application.attr('reinstall', 'true');
          application.save();
        }
      });


  },

  '.view-source-btn click': function(el, ev) {

    application = el.closest('tr').data('application');
    bootbox.animate(false);

    var env = '';
    for (var property in application.attr('environment').attr()) {
      env += property + '=' + application.attr('environment').attr(property) + '\n';
    }

    bootbox.alert('<h5>File</h5><p>' + application.attr('filename') + '</p>' + '<h5>Environment Variables</h5><p><pre><code>' + env + '</pre></code></p>' + '<h5>Source</h5><p><pre><code>' + application.attr('source') + '</code></pre></p>');
  }
});
