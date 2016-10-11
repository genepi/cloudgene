// Static Page Controller
AdminHomePage = can.Control({

	"init" : function(element, options) {
		element.hide();
		that= this;

		Counter.findOne({}, function(counter) {
			element.html(can.view('views/admin/home.ejs', {
				counter : counter
			}));
			element.fadeIn();

			$.getJSON("/api/v2/admin/server/statistics", {
				days : 1
			}, function(mydata) {

				$("#new_users").html(mydata[0].users - mydata[mydata.length -1].users);
				$("#total_users").html(mydata[0].users);

				$("#new_jobs").html(mydata[0].completeJobs - mydata[mydata.length -1].completeJobs);
				$("#total_jobs").html(mydata[0].completeJobs);

				that.options.running = Morris.Area({
					element : 'morris-area-chart',
					data : mydata,
					xkey : 'timestamp',
					ykeys : [ 'runningJobs', 'waitingJobs' ],
					labels : [ 'Running Jobs', 'Waiting Jobs' ],
					pointSize : 0,
					hideHover : 'always',
					smooth : 'false',
					resize : true
				});


			});
		}, function(message) {

		});
	},

	'#day_combo change' : function() {

		days = $("#day_combo").val();
		that = this;
		$.getJSON("/api/v2/admin/server/statistics", {
			days : days
		}, function(mydata) {

			$("#new_users").html(mydata[0].users - mydata[mydata.length -1].users);
			$("#total_users").html(mydata[0].users);

			$("#new_jobs").html(mydata[0].completeJobs - mydata[mydata.length -1].completeJobs);
			$("#total_jobs").html(mydata[0].completeJobs);

			that.options.running.setData(mydata);
			//that.options.jobs.setData(mydata);
			//that.options.users.setData(mydata);
		});
	}

});
