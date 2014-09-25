// Static Page Controller

HomePage = can.Control({

	"init" : function(element, options) {
		this.element.hide();
		this.element.html(can.view('/static/home.ejs'));
		this.element.fadeIn();
		if (options.login) {
			new UserLoginForm("#login-form");
		}
		new CounterList("#counter");
	}

});
