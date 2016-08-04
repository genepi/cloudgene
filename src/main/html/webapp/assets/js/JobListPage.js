//model
Job = can.Model({
	findAll: 'GET /api/v2/jobs',
	findOne: 'GET /api/v2/jobs/{id}/status',
	destroy: 'DELETE /api/v2/jobs/{id}',
	update: 'GET /api/v2/jobs/{id}/cancel'
}, {});

// controller
JobListPage = can.Control({

	"init": function(element, options) {
		this.options.refreshers = [];

		var that = this;
		Job.findAll({
		}, function(jobs) {
			$.each(jobs, function(key, job) {
				if (JobRefresher.needsUpdate(job)) {
					refresher = new JobRefresher();
					refresher.setJob(job);
					that.options.refreshers.push(refresher);
				}
			});
			that.element.html(can.view('views/jobs.ejs', jobs));
			that.element.find("#job-list").dataTable({
				paging: true,
				ordering: false,
				info: true,
				searching: false,
				"bLengthChange": false
			});
			that.element.fadeIn();
		}, function(message) {
			new ErrorPage(that.element, {
				status: message.statusText,
				message: message.responseText
			});
		});

	},

	'.icon-trash click': function(el, ev) {
		var that = this;

		job = el.parent().parent().data('job');
		bootbox.animate(false);
		bootbox.confirm("Are you sure you want to delete <b>" + job.attr('id') + "</b>?", function(result) {
			if (result) {

				$("a[data-handler='1']").button('loading');
				$("a[data-handler='0']").hide('hide');

				var that = this;

				job.destroy(function() {
					// go to jobs page
					bootbox.hideAll();
				}, function(message) {
					// show error message
					new ErrorPage(that.element, {
						status: message.statusText,
						message: message.responseText
					});
				});

				return false;

			}

		});

	},

	'.icon-remove click': function(el, ev) {
		var that = this;

		job = el.parent().parent().data('job');
		bootbox.animate(false);
		bootbox.confirm("Are you sure you want to cancel <b>" + job.attr('id') + "</b>?", function(result) {
			if (result) {
				// cancel

				$("a[data-handler='1']").button('loading');
				$("a[data-handler='0']").hide('hide');

				job.save(function() {
					// go to jobs page
					bootbox.hideAll();
				}, function(message) {
					// show error message
					new ErrorPage(that.element, {
						status: message.statusText,
						message: message.responseText
					});
				});

				return false;

			}
		});

	},

	destroy: function() {

		// stops all job refreshers!
		$.each(this.options.refreshers, function(key, refresher) {
			refresher.stop();
		});

		can.Control.prototype.destroy.call(this);
	}

});

JobRefresher = can.Control({

	setJob: function(job) {
		this.job = job;
		this.active = true;
		this.refresh();
	},

	refresh: function() {
		var that = this;
		Job.findOne({
			id: that.job.id
		}, function(currentJob) {
			if (JobRefresher.needsUpdate(currentJob) && that.active) {
				setTimeout(function() {
					that.refresh();
				}, 5000);
			}
		}, function(message) {
			new ErrorPage(that.element, {
				status: message.statusText,
				message: message.responseText
			});
		});

	},

	stop: function() {
		this.active = false;
	}

});

JobRefresher.needsUpdate = function(job) {
	return job.attr("state") == 1 || job.attr("state") == 2 || job.attr("state") == 3;
};
