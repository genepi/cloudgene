// Static Page Controller

RegisterUserPage = can
		.Control({

			"init" : function(element, options) {
				element.hide();
				element.html(can.view("views/register.ejs"));
				element.fadeIn();
			},

			'submit' : function() {

				// username
				username = this.element.find("[name='username']");
				if (username.val() === "") {
					username.closest('.control-group').addClass('error');
					username.closest('.control-group').find('.help-block')
							.html("The username is required.");
					return false;
				} else {
					username.closest('.control-group').find('.help-block')
							.html('');
					username.closest('.control-group').removeClass('error');
				}

				var pattern = new RegExp(/^[a-zA-Z0-9]+$/);
				if (!pattern.test(username.val())) {
					username.closest('.control-group').addClass('error');
					username
							.closest('.control-group')
							.find('.help-block')
							.html(
									"Your username is not valid. Only characters A-Z, a-z and digits 0-9 are acceptable.");
					return false;
				} else {
					username.closest('.control-group').find('.help-block')
							.html('');
					username.closest('.control-group').removeClass('error');
				}

				// fullname
				fullname = this.element.find("[name='full-name']");
				if (fullname.val() === "") {
					fullname.closest('.control-group').addClass('error');
					fullname.closest('.control-group').find('.help-block')
							.html("The full name is required.");
					return false;
				} else {
					fullname.closest('.control-group').find('.help-block')
							.html('');
					fullname.closest('.control-group').removeClass('error');
				}

				// mail
				mail = this.element.find("[name='mail']");
				var pattern2 = new RegExp(
						/^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?$/i);
				if (!pattern2.test(mail.val())) {
					mail.closest('.control-group').addClass('error');
					mail.closest('.control-group').find('.help-block').html(
							"Please enter a valid mail address.");
					return false;
				} else {
					mail.closest('.control-group').find('.help-block').html('');
					mail.closest('.control-group').removeClass('error');
				}

				// password
				newPassword = this.element.find("[name='new-password']");
				confirmNewPassword = this.element
						.find("[name='confirm-new-password']");
				if (newPassword.val() === ""
						|| newPassword.val() != confirmNewPassword.val()) {
					newPassword.closest('.control-group').addClass('error');
					newPassword.closest('.control-group').find('.help-block')
							.html("Please check your passwords.");
					return false;
				} else {
					newPassword.closest('.control-group').find('.help-block')
							.html('');
					newPassword.closest('.control-group').removeClass('error');
				}

				$('#save').button('loading');

				$.ajax({
					url : "users/register",
					type : "POST",
					data : this.element.find("#signon-form").serialize(),
					dataType : 'json',
					success : function(data) {
						if (data.success == true) {
							// shows success
							$('#signon-form').hide();
							$('#success-message').show();

						} else {
							// shows error msg
							username = $('#signon-form').find(
									"[name='username']");
							username.closest('.control-group')
									.addClass('error');
							username.closest('.control-group').find(
									'.help-block').html(data.message);
							$('#save').button('reset')

						}
					},
					error : function(message) {
						alert('failure: ' + message);
						$('#save').button('reset')
					}
				});

				return false;
			}

		});
