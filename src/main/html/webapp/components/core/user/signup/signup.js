import Control from 'can-control';
import $ from 'jquery';

import User from 'models/user';

import template from './signup.stache';


export default Control.extend({

  "init": function(element, options) {
    this.emailRequired = options.appState.attr('emailRequired');
    $(element).hide();
    $(element).html(template({
      emailRequired: options.appState.attr('emailRequired'),
      userEmailDescription: options.appState.attr('userEmailDescription'),
      userWithoutEmailDescription: options.appState.attr('userWithoutEmailDescription')
    }));
    $(element).fadeIn();
  },

  "#anonymous1 click" : function(){
    this.updateEmailControl();
  },

  "#anonymous2 click" : function(){
    this.updateEmailControl();
  },

  "updateEmailControl": function() {
      if (!this.emailRequired){
        var anonymousControl = $(this.element).find("[name='anonymous']:checked");
        var anonymous = (anonymousControl.val() == "1");
        var mail = $(this.element).find("[name='mail']");
        if (anonymous){
          mail.attr('disabled','disabled');
        } else {
          mail.removeAttr('disabled');
        }
      }
   },

  'submit': function(element, event) {
    event.preventDefault();

    var that = this;
    var user = new User();

    // anonymous radiobutton
    var anonymous = false;

    if (!this.emailRequired){
      var anonymousControl = $(element).find("[name='anonymous']:checked");
      anonymous = (anonymousControl.val() == "1");
    }

    // username
    var username = $(element).find("[name='username']");
    var usernameError = user.checkUsername(username.val());
    this.updateControl(username, usernameError);

    // fullname
    var fullname = $(element).find("[name='full-name']");
    var fullnameError = user.checkName(fullname.val());
    this.updateControl(fullname, fullnameError);

    // mail
    var mail = $(element).find("[name='mail']");
    if (!anonymous){
      var mailError = user.checkMail(mail.val());
      this.updateControl(mail, mailError);
    } else {
      this.updateControl(mail, undefined);
    }

    // password
    var newPassword = $(element).find("[name='new-password']");
    var confirmNewPassword = $(element).find("[name='confirm-new-password']");
    var passwordError = user.checkPassword(newPassword.val(), confirmNewPassword.val());
    this.updateControl(newPassword, passwordError);

    if (usernameError || fullnameError || mailError || passwordError) {
      return false;
    }

    $('#save').button('loading');

    $.ajax({
      url: "/api/v2/users/register",
      type: "POST",
      data: $(element).find("#signon-form").serialize(),
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

  },

  updateControl: function(control, error) {
    if (error) {
      control.removeClass('is-valid');
      control.addClass('is-invalid');
      control.closest('.form-group').find('.invalid-feedback').html(error);
    } else {
      control.removeClass('is-invalid');
      control.addClass('is-valid');
      control.closest('.form-group').find('.invalid-feedback').html('');
    }
  }

});
