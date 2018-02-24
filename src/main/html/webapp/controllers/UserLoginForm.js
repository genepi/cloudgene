UserLoginForm = can.Control({

	"init": function(element, options) {
		element.hide();
		element.html(can.view("views/login.ejs"));
		element.fadeIn();
	},

	'submit': function() {

		password = this.element.find("[name='loginPassword']");
		username = this.element.find("[name='loginUsername']");

		$.ajax({
			url: "login",
			type: "POST",
			data: this.element.find("#signin-form").serialize(),
			dataType: 'json',
			success: function(data) {
				if (data.success == true) {

					// login
					var redirect = 'start.html#!run/';
					window.location = redirect;

					//save CSRF token to local storage
					var dataToken = {
						csrf: data.csrf
					};
					localStorage.setItem('cloudgene', JSON.stringify(dataToken));
				} else {
					// shows error
					password.addClass('is-invalid');
					password.closest('.form-group').find('.invalid-feedback').html(data.message);
				}
			},
			error: function(response) {
	      new ErrorPage(that.element, response);
			}
		});

		return false;
	}

});
