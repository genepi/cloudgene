// Static Page Controller

RegisterUserPage = can.Control({

	"init": function(element, options) {
		element.hide();
		element.html(can.view("views/register.ejs"));
		element.fadeIn();
	},

	'submit': function() {

		that = this;
		user = new User();

		// username
		username = this.element.find("[name='username']");
		usernameError = user.checkUsername(username.val());
		this.updateControl(username, usernameError);

		// fullname
		fullname = this.element.find("[name='full-name']");
		fullnameError = user.checkName(fullname.val());
		this.updateControl(fullname, fullnameError);

		// mail
		mail = this.element.find("[name='mail']");
		mailError = user.checkMail(mail.val());
		this.updateControl(mail, mailError);

		// password
		newPassword = this.element.find("[name='new-password']");
		confirmNewPassword = this.element.find("[name='confirm-new-password']");
		passwordError = user.checkPassword(newPassword.val(), confirmNewPassword.val());
		this.updateControl(newPassword, passwordError);

		if (usernameError || fullnameError ||  mailError || passwordError){
			return false;
		}

		$('#save').button('loading');

		$.ajax({
			url: "/api/v2/users/register",
			type: "POST",
			data: this.element.find("#signon-form").serialize(),
			dataType: 'json',
			success: function(data) {
				if (data.success == true) {
					// shows success
					$('#signon-form').hide();
					$('#success-message').show();
				} else {
					// shows error msg
					username = $('#signon-form').find("[name='username']");
					that.updateControl(username, data.message);
					$('#save').button('reset');

				}
			},
			error: function(message) {
				alert('failure: ' + message);
				$('#save').button('reset');
			}
		});

		return false;
	},

	updateControl: function(control, error){
		if (error){
			control.removeClass('is-valid');
			control.addClass('is-invalid');
			control.closest('.form-group').find('.invalid-feedback').html(error);
		}else{
			control.removeClass('is-invalid');
			control.addClass('is-valid');
			control.closest('.form-group').find('.invalid-feedback').html('');
		}
	}

});
