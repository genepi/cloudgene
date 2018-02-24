UpdateProfilePage = can.Control({

	"init": function(element, options) {
		element.hide();
		that = this;
		User.findOne({
			user: 'me'
		}, function(user) {
			element.html(can.view('views/profile.ejs', {
				user: user
			}));
			that.options.user = user;
			element.fadeIn();
		});
	},

	'submit': function() {

		user = new User();

		fullName = this.element.find("[name='full-name']");
		mail = this.element.find("[name='mail']");

		// fullname
		fullname = this.element.find("[name='full-name']");
		fullnameError = user.checkName(fullname.val());
		this.updateControl(fullname, fullnameError);

		// mail
		mail = this.element.find("[name='mail']");
		mailError = user.checkMail(mail.val());
		this.updateControl(mail, mailError);

		// password if password is not empty. else no password update on server side
		newPassword = this.element.find("[name='new-password']");
		newPasswordError = undefined;
		if (newPassword.val() !== "") {
			confirmNewPassword = this.element.find("[name='confirm-new-password']");
			newPasswordError = user.checkPassword(newPassword.val(), confirmNewPassword.val());
			this.updateControl(confirmNewPassword, newPasswordError);
		}

		if (fullnameError ||  mailError || newPasswordError){
			return false;
		}

		$.ajax({
			url: "/api/v2/users/me/profile",
			type: "POST",
			data: this.element.find("#account-form").serialize(),
			dataType: 'json',
			success: function(data) {

				if (data.success == true) {

					// shows okey
					$("#error-message").hide();
					$("#account-form").hide();
					$("#success-message").show();
					$("#success-message").html(data.message);

				} else {
					// shows error
					$("#error-message").show();
					$("#success-message").hide();
					$("#error-message").html(data.message);

				}
			},
			error: function(response) {
	      new ErrorPage(that.element, response);
			}
		});

		return false;
	},

	'#create_token click': function() {

		user = that.options.user;

		userToken = new UserToken();
		userToken.attr('user', user.attr('username'));

		userToken.save(function(responseText) {
			user.attr('hasApiToken', true);
			bootbox.alert('<h4>API Token</h4>Your token for this service is:<br><textarea style="width:100%;height:100px;">' + responseText.token + '</textarea>');
		}, function(message) {
			bootbox.alert('<h4>API Token</h4>Error: ' + message);
		});

	},

	'#revoke_token click': function() {

		user = that.options.user;

		bootbox.confirm("Are you sure you want to revoke your <b>API Token</b>? All your applications and scripts that are you using this API token have to be changed!", function(result) {

			if (result) {
				userToken = new UserToken();
				userToken.attr('user', user.attr('username'));
				userToken.attr('id', 'luki');
				userToken.destroy(function() {
					user.attr('hasApiToken', false);
					bootbox.alert('<h4>API Token</h4>Your token is now inactive.');
				}, function() {
					bootbox.alert('<h4>API Token</h4>Error: ' + message);
				});
			}
		});
	},

	'#show_token click': function() {
		user = that.options.user;

		UserToken.findOne({
			user: user.attr('username')
		}, function(responseText) {
			user.attr('hasApiToken', true);
			bootbox.alert('<h4>API Token</h4>Your token for this service is:<br><textarea style="width:100%;height:100px;">' + responseText.token + '</textarea>');
		}, function(message) {
			bootbox.alert('<h4>API Token</h4>Error: ' + message);
		});

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
