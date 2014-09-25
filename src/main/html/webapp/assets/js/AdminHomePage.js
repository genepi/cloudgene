// Static Page Controller

AdminHomePage = can.Control({

	"init" : function(element, options) {
		element.hide();
		element.html(can.view('/views/admin/home.ejs'));
		element.fadeIn();
		
		new CounterList("#counter");

		$.getJSON("/statistics", function(mydata) {

			Morris.Area({
				element : 'morris-area-chart3',
				data : mydata,
				xkey : 'timestamp',
				ykeys : [ 'completeChromosomes' ],
				labels : [ 'Complete Chromosomes' ],
				pointSize : 2,
				hideHover : 'auto',
				resize : true
			});

			Morris.Area({
				element : 'morris-area-chart',
				data : mydata,
				xkey : 'timestamp',
				ykeys : [ 'runningJobs', 'waitingJobs' ],
				labels : [ 'Running Jobs', 'Waiting Jobs' ],
				pointSize : 2,
				hideHover : 'auto',
				resize : true
			});

			Morris.Area({
				element : 'morris-area-chart2',
				data : mydata,
				xkey : 'timestamp',
				ykeys : [ 'runningChromosomes', 'waitingChromosomes' ],
				labels : [ 'Running Chromosomes', 'Waiting Chromosomes' ],
				pointSize : 2,
				hideHover : 'auto',
				resize : true
			});

			Morris.Area({
				element : 'morris-area-chart4',
				data : mydata,
				xkey : 'timestamp',
				ykeys : [ 'completeJobs' ],
				labels : [ 'Complete Jobs' ],
				pointSize : 2,
				hideHover : 'auto',
				resize : true
			});

			Morris.Area({
				element : 'morris-area-chart5',
				data : mydata,
				xkey : 'timestamp',
				ykeys : [ 'users' ],
				labels : [ 'Users' ],
				pointSize : 2,
				hideHover : 'auto',
				resize : true
			});

		});
	}

});
