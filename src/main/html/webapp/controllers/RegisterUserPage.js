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
		error = user.checkUsername(username.val());
		this.updateControl(username, error);

		// fullname
		fullname = this.element.find("[name='full-name']");
		error = user.checkName(fullname.val());
		this.updateControl(fullname, error);

		// mail
		mail = this.element.find("[name='mail']");
		error = user.checkMail(mail.val());
		this.updateControl(mail, error);

		// password
		newPassword = this.element.find("[name='new-password']");
		confirmNewPassword = this.element.find("[name='confirm-new-password']");
		error = user.checkPassword(newPassword.val(), confirmNewPassword.val());
		this.updateControl(newPassword, error);


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
