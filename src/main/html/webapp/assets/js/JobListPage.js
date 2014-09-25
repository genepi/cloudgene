//model
Job = can.Model({
	findAll : 'GET /{mode}jobs',
	findOne : 'POST /jobs/newstate',
	destroy : 'POST /jobs/delete',
	update : 'POST /jobs/cancel'
}, {});

// controller
JobListPage = can.Control({

	"init" : function(element, options) {
		this.options.refreshers = new Array();

		var that = this;
		Job.findAll({
			mode : ''
		}, function(jobs) {
			$.each(jobs, function(key, job) {
				if (JobRefresher.needsUpdate(job)) {
					refresher = new JobRefresher();
					refresher.setJob(job);
					that.options.refreshers.push(refresher);
				}
			});
			that.element.html(can.view('/views/jobs.ejs', jobs));
			that.element.find("#job-list").dataTable({
				paging : true,
				ordering : false,
				info : true,
				searching : false,
				"bLengthChange" : false
			});
			that.element.fadeIn();
		}, function(message) {
			new ErrorPage(that.element, {
				status : message.statusText,
				message : message.responseText
			});
		});

	},

	'.icon-trash click' : function(el, ev) {

		job = el.parent().parent().data('job');
		bootbox.animate(false);
		bootbox.confirm("Are you sure you want to delete <b>" + job.attr('id')
				+ "</b>?", function(result) {
			if (result) {
				job.destroy();
			}
		});

	},

	'.icon-remove click' : function(el, ev) {

		job = el.parent().parent().data('job');
		bootbox.animate(false);
		bootbox.confirm("Are you sure you want to cancel <b>" + job.attr('id')
				+ "</b>?", function(result) {
			if (result) {
				// cancel
				job.save();
			}
		});

	},

	destroy : function() {

		// stops all job refreshers!
		$.each(this.options.refreshers, function(key, refresher) {
			refresher.stop();
		});
		console.log("stop");

		can.Control.prototype.destroy.call(this);
	}

});

JobRefresher = can.Control({

	setJob : function(job) {
		this.job = job;
		this.active = true;
		this.refresh();
	},

	refresh : function() {
		var that = this;
		Job.findOne({
			job_id : that.job.id
		}, function(currentJob) {
			if (JobRefresher.needsUpdate(currentJob) && that.active) {
				setTimeout(function() {
					that.refresh();
				}, 5000);
			}
		}, function(message) {
			new ErrorPage(that.element, {
				status : message.statusText,
				message : message.responseText
			});
		});

	},

	stop : function() {
		this.active = false;
	}

});

JobRefresher.needsUpdate = function(job) {

	return job.attr("state") == 1 || job.attr("state") == 2
			|| job.attr("state") == 3;
}