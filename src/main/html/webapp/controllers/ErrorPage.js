// Error Page Controller

ErrorPage = can.Control({

	"init" : function(element, options) {
		this.element.hide();
		this.element.html(can.view('views/error.ejs', {
			error : {
				statusText : options.status,
				responseText : options.message
			}
		}));
		this.element.fadeIn();
	}

});
