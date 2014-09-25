//model
JobParameter = can.Model({
	findAll : 'GET /apps/params'
}, {

});

//controller
Application = can.Model({
	findOne : 'GET /app'
}, {

});

// controller
SubmitJobPage = can.Control({

	"init" : function(element, options) {
		
		Application.findOne({}, function(details) {
			element.hide();
			element.html(can.view('/views/run.ejs', {
				details : details
			}))
			buildForm("#parameters", details.params, details.submitButton)
			element.fadeIn();
			
		}, function(message) {
			new ErrorPage(that.element, {
				status : message.statusText,
				message : message.responseText
			});
		});
		
	},

	'submit' : function(tr) {

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
					return $(this).data('required')
							&& $(this).val() === "---empty---"
							&& $(this).attr('disabled') != 'disabled';
				}).closest('.control-group').addClass('error');

		this.element.find('select').filter(
				function() {
					return $(this).data('required')
							&& $(this).val() === "---empty---"
							&& $(this).attr('disabled') != 'disabled';
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
					return $(this).data('required') && $(this).val() === ""
							&& $(this).attr('disabled') != 'disabled';
				}).closest('.control-group').addClass('error');

		this.element.find('input').filter(
				function() {
					return $(this).data('required') && $(this).val() === ""
							&& $(this).attr('disabled') != 'disabled';
				}).closest('.control-group').find('.help-block').html(
				"This parameter is required.");

		if (faults.length > 0 || faults2.length > 0)
			return false; // if any required are empty, cancel submit

		$("#waiting-dialog").modal();

		this.element.find("#parameters").ajaxSubmit({
			dataType : 'json',
			success : function(answer) {
				if (answer.success) {
					$("#waiting-dialog").modal('hide');
					can.route('jobs/:job');
					can.route.attr({
						route : 'jobs/:job',
						job : answer.id,
						page : 'jobs'
					});
				} else {
					$("#waiting-text").html("error!!!");
				}
			},

			error : function(answer) {
				$("#waiting-text").html("error!!!");
			},

			uploadProgress : function(event, position, total, percentComplete) {
				$("#waiting-progress").css("width", percentComplete + "%");
				console.log(percentComplete + "%");
			}
		});

		return false;
	}

});
