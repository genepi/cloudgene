// Static Page Controller

NewPasswordPage = can
		.Control({

			"init" : function(element, options) {
				element.hide();
				element.html(can.view('views/update-password.ejs', {
					data : options.data
				}));
				element.fadeIn();
			},

			'submit' : function() {

				// password
				newPassword = this.element.find("[name='new-password']");
				confirmNewPassword = this.element
						.find("[name='confirm-new-password']");
				if (newPassword.val() === ""
						|| newPassword.val() != confirmNewPassword.val()) {
					confirmNewPassword.closest('.control-group').addClass(
							'error');
					confirmNewPassword.closest('.control-group').find(
							'.help-block').html("Please check your passwords.");
					return false;
				} else {
					confirmNewPassword.closest('.control-group').find(
							'.help-block').html('');
					confirmNewPassword.closest('.control-group').removeClass(
							'error');
				}

				$.ajax({
					url : "users/update-password",
					type : "POST",
					data : this.element.find("#update-password-form")
							.serialize(),
					dataType : 'json',
					success : function(data) {

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
					error : function(message) {
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
