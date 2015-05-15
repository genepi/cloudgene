//model
Cluster = can.Model({
	findOne : 'GET cluster'
}, {

});

// controller
AdminServerPage = can.Control({

	"init" : function(element, options) {
		element.hide();
		Cluster.findOne({}, function(cluster) {
			element.html(can.view('views/admin/server.ejs', {
				cluster : cluster
			}));
			element.fadeIn();
		});

	}

});
