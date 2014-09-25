UserLoginForm = can.Control({

	"init" : function(element, options) {
		element.hide();
		element.html(can.view("/views/login.ejs"));
		element.fadeIn();
	},

	'submit' : function() {

		password = this.element.find("[name='loginPassword']");
		username = this.element.find("[name='loginUsername']");

		$.ajax({
			url : "/login",
			type : "POST",
			data : this.element.find("#signin-form").serialize(),
			dataType : 'json',
			success : function(data) {
				if (data.success == true) {

					// login
					var redirect = 'start.html';
					window.location = redirect + window.location.hash

				} else {
					// shows error
					username.closest('.control-group').addClass('error');
					password.closest('.control-group').addClass('error');
					password.closest('.control-group').find('.help-block')
							.html("Wrong username or password.");

				}
			},
			error : function(message) {
				new ErrorPage(that.element, {
					status : message.statusText,
					message : message.responseText
				});
			}
		});

		return false;
	}

});