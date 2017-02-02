// Static Page Controller

RegisterUserPage = can.Control({

	"init": function(element, options) {
		element.hide();
		element.html(can.view("views/register.ejs"));
		element.fadeIn();
	},

	'submit': function() {

		user = new User();

		// username
		username = this.element.find("[name='username']");
		error = user.checkUsername(username.val());
		if (error) {
			username.closest('.control-group').addClass('error');
			username.closest('.control-group').find('.help-block').html(error);
			return false;
		} else {
			username.closest('.control-group').find('.help-block').html('');
			username.closest('.control-group').removeClass('error');
		}

		// fullname
		fullname = this.element.find("[name='full-name']");
		error = user.checkName(fullname.val());
		if (error) {
			fullname.closest('.control-group').addClass('error');
			fullname.closest('.control-group').find('.help-block').html(error);
			return false;
		} else {
			fullname.closest('.control-group').find('.help-block').html('');
			fullname.closest('.control-group').removeClass('error');
		}

		// mail
		mail = this.element.find("[name='mail']");
		error = user.checkMail(mail.val());
		if (error) {
			mail.closest('.control-group').addClass('error');
			mail.closest('.control-group').find('.help-block').html(error);
			return false;
		} else {
			mail.closest('.control-group').find('.help-block').html('');
			mail.closest('.control-group').removeClass('error');
		}

		// password
		newPassword = this.element.find("[name='new-password']");
		confirmNewPassword = this.element.find("[name='confirm-new-password']");
		error = user.checkPassword(newPassword.val(), confirmNewPassword.val());
		if (error) {
			newPassword.closest('.control-group').addClass('error');
			newPassword.closest('.control-group').find('.help-block').html(error);
			return false;
		} else {
			newPassword.closest('.control-group').find('.help-block').html('');
			newPassword.closest('.control-group').removeClass('error');
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
					username.closest('.control-group').addClass('error');
					username.closest('.control-group').find('.help-block').html(data.message);
					$('#save').button('reset');

				}
			},
			error: function(message) {
				alert('failure: ' + message);
				$('#save').button('reset');
			}
		});

		return false;
	}

});
