// Static Page Controller

NewPasswordPage = can
	.Control({

		"init": function(element, options) {
			element.hide();
			element.html(can.view('views/update-password.ejs', {
				data: options.data
			}));
			element.fadeIn();
		},

		'submit': function() {

			// password
			newPassword = this.element.find("[name='new-password']");
			confirmNewPassword = this.element.find("[name='confirm-new-password']");
			confirmNewPassword.closest('.control-group').find('.help-block').html('');
			confirmNewPassword.closest('.control-group').removeClass('error');
			if (newPassword.val() === "" || newPassword.val() != confirmNewPassword.val()) {
				confirmNewPassword.closest('.control-group').addClass('error');
				confirmNewPassword.closest('.control-group').find('.help-block')
					.html("Please check your passwords.");
				return false;
			}

			if (newPassword.val().length < 6) {
				confirmNewPassword.closest('.control-group').addClass('error');
				confirmNewPassword.closest('.control-group').find('.help-block')
					.html("Password must contain at least six characters!");
				return false;
			}
			re = /[0-9]/;
			if (!re.test(newPassword.val())) {
				confirmNewPassword.closest('.control-group').addClass('error');
				confirmNewPassword.closest('.control-group').find('.help-block')
					.html("Password must contain at least one number (0-9)!");
				return false;
			}
			re = /[a-z]/;
			if (!re.test(newPassword.val())) {
				confirmNewPassword.closest('.control-group').addClass('error');
				confirmNewPassword.closest('.control-group').find('.help-block')
					.html("Password must contain at least one lowercase letter (a-z)!");
				return false;
			}
			re = /[A-Z]/;
			if (!re.test(newPassword.val())) {
				confirmNewPassword.closest('.control-group').addClass('error');
				confirmNewPassword.closest('.control-group').find('.help-block')
					.html("Password must contain at least one uppercase letter (A-Z)!");
				return false;
			}

			$.ajax({
				url: "users/update-password",
				type: "POST",
				data: this.element.find("#update-password-form")
					.serialize(),
				dataType: 'json',
				success: function(data) {

					if (data.success == true) {

						// shows okey
						$("#update-page").hide();
						$("#error-message").hide();
						$("#success-message").show();
						$("#success-message").html(data.message);

					} else {
						// shows error
						$("#update-page").hide();
						$("#error-message").show();
						$("#success-message").hide();
						$("#error-message").html(data.message);

					}
				},
				error: function(message) {
					// shows error
					$("#error-message").show();
					$("#success-message").hide();
					$("#error-message").html(
						message.status + ": " + message.statusText);

				}
			});

			return false;

		}

	});
