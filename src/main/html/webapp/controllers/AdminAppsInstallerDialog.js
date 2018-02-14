// controller
AdminAppsInstallerDialog = can.Control({

  "init": function(element, options) {

  },

  '.install-app-btn click': function(el, ev) {

    app = new Application();
    app.attr('name', el.data('app-id'));
    app.attr('url', el.data('app-url'));

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
  },

});
