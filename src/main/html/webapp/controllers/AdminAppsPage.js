// controller
AdminAppsPage = can.Control({

	"init": function(element, options) {
		var that = this;

		Application.findAll({}, function(applications) {

			that.element.html(can.view('views/admin/apps.ejs', {
				applications: applications
			}));
			$("#content").fadeIn();

		});

	},

	'#install-app-btn click': function(el, ev) {
		bootbox.animate(false);
		bootbox.confirm(
			'<h4>Install App from URL</h4><p>Please enter the URL of the zip file of the application.</p><form><input class="field span5" id="url" name="url" value="http://"/></form>',
			function(result) {
				if (result) {
					var url = $('#url').val();
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

	'.icon-trash click': function(el, ev) {

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

	'.icon-pencil click': function(el, ev) {

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


	}

});
