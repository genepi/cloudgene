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

    // password if password is not empty. else no password update on server side
    newPassword = this.element.find("[name='new-password']");
    if (newPassword.val() !== "") {
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
          $("#success-message").show();
          $("#success-message").html(data.message);

        } else {
          // shows error
          $("#error-message").show();
          $("#success-message").hide();
          $("#error-message").html(data.message);

        }
      },
      error: function(message) {
        new ErrorPage(that.element, {
          status: message.statusText,
          message: message.responseText
        });

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

  '#delete_account click': function() {

    var that = this;
    user = that.options.user;

    var deleteAcountDialog = bootbox.dialog(can.view('views/delete-account.ejs', {}), [

      {
        label: "Cancel",
        class: "btn-default",
        callback: function() {}
      },

      {
        label: "Delete Account",
        class: "btn-danger",
        callback: function() {

          // get form parameters
          var form = deleteAcountDialog.find("form");
          var values = can.deparam(form.serialize());

          // create delete request
          userProfile = new UserProfile();
          userProfile.attr('user', 'me');
          userProfile.attr('username', values['username']);
          userProfile.attr('password', values['password']);
          userProfile.attr('id', 'id');
          userProfile.destroy(function() {
            bootbox.alert('<h4>Account deleted</h4>Your account is now deleted.');
            window.location.href = 'logout';
            return true;
          }, function(message) {
            response = JSON.parse(message.responseText);
            bootbox.alert('<h4>Account not deleted</h4>Error: ' + response.message);
            return false;
          });
        }
      }
    ]);
  }



});
