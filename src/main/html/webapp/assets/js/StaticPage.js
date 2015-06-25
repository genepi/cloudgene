// Static Page Controller

StaticPage = can.Control({

	"init" : function(element, options) {
		element.hide();
		try{
			view = can.view(options.template);
			element.html(view);
		}catch(e){
			element.html(can.view('views/error.ejs', {
				error : {
					statusText : "404",
					responseText : "Page " + options.template + " not found."
				}
			}));
		}
		element.fadeIn();
	}

});
