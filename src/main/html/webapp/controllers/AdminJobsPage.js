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

		job = el.parent().parent().data('job');
		bootbox.animate(false);
		bootbox.confirm("Are you sure you want to delete <b>" + job.attr('id') + "</b>?", function(result) {
			if (result) {
				job.destroy();
			}
		});

	},

	'.icon-remove click': function(el, ev) {

		job = el.parent().parent().data('job');
		bootbox.animate(false);
		bootbox.confirm("Are you sure you want to cancel <b>" + job.attr('id') + "</b>?", function(result) {
			if (result) {
				// cancel
				job.save();
			}
		});

	}

});
