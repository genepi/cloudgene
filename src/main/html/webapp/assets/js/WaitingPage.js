// WaitingPage Controller

WaitingPage = can.Control({

	"init" : function(element, options) {
		this.element.hide();
		this.element.html(can.view('views/waiting.ejs', {
			dialog : {
				title : "Please wait...",
				description : ""
			}
		}));
		this.element.fadeIn();
	}

});
