import Control from 'can-control';
import $ from 'jquery';

import User from 'models/user';

import template from './password-recovery.ejs';


export default Control.extend({

  "init": function(element, options) {
    $(element).hide();
    $(element).html(template({
      data: options.data
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
      confirmNewPassword.closest('.control-group').addClass('error');
      confirmNewPassword.closest('.control-group').find('.help-block').html(error);
      return false;
    } else {
      confirmNewPassword.closest('.control-group').find('.help-block').html('');
      confirmNewPassword.closest('.control-group').removeClass('error');
    }

    $.ajax({
      url: "/api/v2/users/update-password",
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
