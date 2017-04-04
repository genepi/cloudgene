// controller
SubmitJobPage = can.Control({

	"init": function(element, options) {
		that = this;
		Application.findOne({
			tool: options.tool
		}, function(details) {
			element.hide();
			element.html(can.view('views/run.ejs', {
				details: details
			}));
			buildForm(options.tool, "#parameters", details.params, details.submitButton);
			element.fadeIn();

		}, function(message) {
			new ErrorPage(that.element, {
				status: message.statusText,
				message: message.responseText
			});
		});

	},

	'submit': function(tr) {
		try {
			tool = this.element.find('tool');

			console.log("submit job...");

			// ---- listboxes

			// reset

			this.element.find('select').filter(function() {
				return true;
			}).closest('.control-group').removeClass("error");

			this.element.find('select').filter(function() {
				return true;
			}).closest('.control-group').find('.help-block').html("");

			// validation

			var faults2 = this.element.find('select').filter(
				function() {
					return $(this).data('required') && $(this).val() === "---empty---" && $(this).attr('disabled') != 'disabled';
				}).closest('.control-group').addClass('error');

			this.element.find('select').filter(
				function() {
					return $(this).data('required') && $(this).val() === "---empty---" && $(this).attr('disabled') != 'disabled';
				}).closest('.control-group').find('.help-block').html(
				"This parameter is required.");

			// --- input fields

			// reset
			this.element.find('input').filter(function() {
				return true;
			}).closest('.control-group').removeClass("error");

			this.element.find('input').filter(function() {
				return true;
			}).closest('.control-group').find('.help-block').html("");

			// validation

			var faults = this.element.find('input').filter(
				function() {
					return $(this).data('required') && $(this).val() === "" && $(this).attr('disabled') != 'disabled';
				}).closest('.control-group').addClass('error');

			this.element.find('input').filter(
				function() {
					return $(this).data('required') && $(this).val() === "" && $(this).attr('disabled') != 'disabled';
				}).closest('.control-group').find('.help-block').html(
				"This parameter is required.");


			// agb checkboxes

			// reset

			var faults3 = this.element.find('input').filter(
				function() {
					return $(this).attr('type') === 'checkbox' && $(this).attr('class') === 'agb' && $(this).attr('checked') != 'checked';
				}).closest('.control-group').addClass('error');

			this.element.find('input').filter(
				function() {
					return $(this).attr('type') === 'checkbox' && $(this).attr('class') === 'agb' && $(this).attr('checked') != 'checked';
				}).closest('.control-group').find('.help-block').html("Please agree to the terms and conditions.");

			if (faults.length > 0 || faults2.length > 0 || faults3.length > 0)
				return false; // if any required are empty, cancel submit

			$("#waiting-dialog").modal();
			var csrfToken;
			if (localStorage.getItem("cloudgene")) {
				try {

					// get data
					var data = JSON.parse(localStorage.getItem("cloudgene"));
					csrfToken = data.csrf;

				} catch (e) {

				}
			}

			this.element.find("#parameters").ajaxSubmit({
				dataType: 'json',

				headers: {
					"X-CSRF-Token": csrfToken
				},

				success: function(answer) {
					if (answer.success) {
						$("#waiting-dialog").modal('hide');
						can.route('jobs/:job');
						can.route.attr({
							route: 'jobs/:job',
							job: answer.id,
							page: 'jobs'
						});
					} else {
						$("#waiting-dialog").modal('hide');
						new ErrorPage("#content", {
							status: "",
							message: answer.message
						});

					}
				},

				error: function(message) {
					$("#waiting-dialog").modal('hide');
					new ErrorPage("#content", {
						status: message.statusText,
						message: message.responseText
					});

				},

				uploadProgress: function(event, position, total, percentComplete) {
					$("#waiting-progress").css("width", percentComplete + "%");
					console.log(percentComplete + "%");
				}
			});
		} catch (e) {
			console.log(e);
		}
		return false;
	}

});
