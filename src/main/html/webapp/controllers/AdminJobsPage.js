// controller
AdminJobsPage = can.Control({

	"init": function(element, options) {
		element.html(can.view('views/admin/jobs.ejs'));
		element.fadeIn();

		this.loadJobs("#job-list-running-stq", "running-stq");
		this.loadJobs("#job-list-running-ltq", "running-ltq");
		this.loadJobs("#job-list-current", "current");


	},

	loadJobs: function(element, mySate) {
		var that = this;
		JobAdminDetails.findAll({
			state: mySate
		}, function(jobs) {
			$(element).html(can.view('views/admin/jobs-list.ejs', jobs));
		}, function(message) {
			new ErrorPage(that.element, {
				status: message.statusText,
				message: message.responseText
			});
		});

	},

	'.icon-trash click': function(el, ev) {

		job = el.closest('tr').data('job');
		bootbox.animate(false);
		bootbox.confirm("Are you sure you want to delete <b>" + job.attr('id') + "</b>?", function(result) {
			if (result) {
				job.destroy();
			}
		});

	},

	'.icon-remove click': function(el, ev) {

		job = el.closest('tr').data('job');
		bootbox.animate(false);
		bootbox.confirm("Are you sure you want to cancel <b>" + job.attr('id') + "</b>?", function(result) {
			if (result) {
				// cancel
				job.save();
			}
		});

	},

	'.icon-arrow-up click': function(el, ev) {
		job = el.closest('tr').data('job');
		that = this;
		request = $.get('api/v2/admin/jobs/' + job.attr('id') + '/priority');
		request.success(function(data) {
			bootbox.alert(data);
			that.init(that.element, that.options);
		});
		request.error(function(data) {
			bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
		});
	},

	'.icon-time click': function(el, ev) {
		job = el.closest('tr').data('job');
		that = this;
		request = $.get('api/v2/admin/jobs/' + job.attr('id') + '/reset');
		request.success(function(data) {
			bootbox.alert(data);
			that.init(that.element, that.options);
		});
		request.error(function(data) {
			bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
		});
	},

	'.icon-fire click': function(el, ev) {
		job = el.closest('tr').data('job');
		that = this;
		request = $.get('api/v2/admin/jobs/' + job.attr('id') + '/retire');
		request.success(function(data) {
			bootbox.alert(data);
			that.init(that.element, that.options);
		});
		request.error(function(data) {
			bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
		});
	},

	'.icon-plus click': function(el, ev) {
		job = el.closest('tr').data('job');
		that = this;
		request = $.get('api/v2/admin/jobs/' + job.attr('id') + '/change-retire/1');
		request.success(function(data) {
			bootbox.alert(data);
			that.init(that.element, that.options);
		});
		request.error(function(data) {
			bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
		});
	}

});
