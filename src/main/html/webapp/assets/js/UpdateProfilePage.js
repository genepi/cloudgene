//model
User = can.Model({
	findOne : 'GET /users/details',
	destroy : 'POST /users/delete',
	findAll : 'GET /users'
}, {});

UpdateProfilePage = can
		.Control({

			"init" : function(element, options) {
				element.hide();
				User.findOne({}, function(user) {
					element.html(can.view('/views/profile.ejs', {
						user : user
					}));
					element.fadeIn();
				});
			},

			'submit' : function() {

				fullName = this.element.find("[name='full-name']");
				mail = this.element.find("[name='mail']");

				if (fullName.val() === "") {
					$("#error-message").show();
					$("#success-message").hide();
					$("#error-message").html("Please enter your name.");
					return false;
				}

				if (mail.val() === "") {
					$("#error-message").show();
					$("#success-message").hide();
					$("#error-message")
							.html("Please enter your e-mail address");
					return false;
				}

				var pattern2 = new RegExp(
						/^((([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+(\.([a-z]|\d|[!#\$%&'\*\+\-\/=\?\^_`{\|}~]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])+)*)|((\x22)((((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(([\x01-\x08\x0b\x0c\x0e-\x1f\x7f]|\x21|[\x23-\x5b]|[\x5d-\x7e]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(\\([\x01-\x09\x0b\x0c\x0d-\x7f]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))))*(((\x20|\x09)*(\x0d\x0a))?(\x20|\x09)+)?(\x22)))@((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.)+(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?$/i);
				if (!pattern2.test(mail.val())) {
					$("#error-message").show();
					$("#success-message").hide();
					$("#error-message").html(
							"Please enter a valid e-mail address");
					return false;
				}

				$.ajax({
					url : "/users/update2",
					type : "POST",
					data : this.element.find("#account-form").serialize(),
					dataType : 'json',
					success : function(data) {

						if (data.success == true) {

							// shows okey
							$("#error-message").hide();
							$("#success-message").show();
							$("#success-message").html(data.message);

						} else {
							// shows error
							$("#error-message").show();
							$("#success-message").hide();
							$("#error-message").html(data.message);

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
