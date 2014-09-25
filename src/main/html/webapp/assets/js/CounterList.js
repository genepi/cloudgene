//model
Counter = can.Model({
	findOne : 'GET /counters'
}, {

});

// controller
CounterList = can.Control({

	"init" : function(element, options) {
		var that = this;
		Counter.findOne({}, function(counter) {
			that.element.html(can.view('/views/counter.ejs', {
				counter : counter
			}));
		}, function(message) {

		});

	}

});