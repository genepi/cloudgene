import Control from 'can-control';
import $ from 'jquery';

import User from 'models/user';

import template from './password-recovery.stache';


export default Control.extend({

  "init": function(element, options) {
    $(element).hide();
    $(element).html(template({
      user: options.user,
      key: options.key
    }));
    $(element).fadeIn();
  },

  'submit': function(element, event) {
    event.preventDefault();

    var user = new User();

    // password
    var newPassword = $(element).find("[name='new-password']");
    var confirmNewPassword = $(element).find("[name='confirm-new-password']");
    var error = user.checkPassword(newPassword.val(), confirmNewPassword.val());

    if (error) {
      confirmNewPassword.removeClass('is-valid');
      confirmNewPassword.addClass('is-invalid');
      confirmNewPassword.closest('.form-group').find('.invalid-feedback').html(error);
      return false;
    } else {
      confirmNewPassword.removeClass('is-invalid');
      confirmNewPassword.addClass('is-valid');
      confirmNewPassword.closest('.form-group').find('.invalid-feedback').html('');
    }


    $.ajax({
      url: "api/v2/users/update-password",
      type: "POST",
      data: $(element).find("#update-password-form")
        .serialize(),
      dataType: 'json',
      success: function(response) {

        if (response.success == true) {

          // shows okey
          $("#update-page").hide();
          $("#error-message").hide();
          $("#success-message").show();
          $("#success-message").html(response.message);

        } else {
          // shows error
          $("#update-page").hide();
          $("#error-message").show();
          $("#success-message").hide();
          $("#error-message").html(response.message);

        }
      },
      error: function(response) {
        // shows error
        $("#error-message").show();
        $("#success-message").hide();
        $("#error-message").html(response.status + ": " + response.statusText);

      }
    });

  }

});
