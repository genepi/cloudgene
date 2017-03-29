// controller
AdminServerPage = can.Control({

	"init": function(element, options) {
		element.hide();
		Cluster.findOne({}, function(cluster) {
			element.html(can.view('views/admin/server.ejs', {
				cluster: cluster
			}));
			element.fadeIn();
		});

	},

	"#maintenance-enter-btn click": function() {
		that = this;
		request = $.get('api/v2/admin/server/maintenance/enter');
		request.success(function(data) {
			bootbox.alert(data);
			that.init(that.element, that.options);
		});
		request.error(function(data) {
			bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
		});
	},

	"#maintenance-exit-btn click": function() {
		that = this;
		request = $.get('api/v2/admin/server/maintenance/exit');
		request.success(function(data) {
			bootbox.alert(data);
			that.init(that.element, that.options);
		});
		request.error(function(data) {
			bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
		});
	},

	"#queue-block-btn click": function() {
		that = this;
		request = $.get('api/v2/admin/server/queue/block');
		request.success(function(data) {
			bootbox.alert(data);
			that.init(that.element, that.options);
		});
		request.error(function(data) {
			bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
		});
	},

	"#queue-open-btn click": function() {
		that = this;
		request = $.get('api/v2/admin/server/queue/open');
		request.success(function(data) {
			bootbox.alert(data);
			that.init(that.element, that.options);
		});
		request.error(function(data) {
			bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
		});
	},

	"#retire-btn click": function() {
		that = this;
		request = $.get('api/v2/admin/jobs/retire');
		request.success(function(data) {
			bootbox.alert(data);
			that.init(that.element, that.options);
		});
		request.error(function(data) {
			bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
		});
	}


});
