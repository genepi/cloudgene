// Error 404 Page Controller

Error404Page = can.Control({

	"init" : function(element, options) {
		this.element.hide();
		this.element.html(can.view('views/error.ejs', {
			error : {
				statusText : "404",
				responseText : "Page not found."
			}
		}));
		this.element.fadeIn();
	}

});
