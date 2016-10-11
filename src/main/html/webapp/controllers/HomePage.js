// Static Page Controller
HomePage = can.Control({

	"init" : function(element, options) {
		that = this;
		Counter.findOne({}, function(counter) {
			that.element.hide();
			that.element.html(can.view('static/home.ejs',{counter: counter, loggedIn: !options.login}));
			that.element.fadeIn();
			if (options.login) {
				new UserLoginForm("#login-form");
			}
		}, function(message) {

		});
	}

});
