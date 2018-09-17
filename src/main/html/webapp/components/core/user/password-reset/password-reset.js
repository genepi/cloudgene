import Control from 'can-control';
import $ from 'jquery';

import template from './password-reset.stache';


export default Control.extend({

  "init": function(element, options) {
    $(element).hide();
    $(element).html(template());
    $(element).fadeIn();
  },

  'submit': function(element, event) {
    event.preventDefault();

    var username = $(element).find("[name='username']");

    $.ajax({
      url: "/api/v2/users/reset",
      type: "POST",
      data: $(element).find("#reset-form").serialize(),
      dataType: 'json',
      success: function(data) {
        if (data.success == true) {

          // show okey
          $("#reset-page").hide();
          $("#success-message").show();
          $("#success-message").html(data.message);

        } else {
          // shows error
          username.addClass('is-invalid');
          username.closest('.form-group').find('.invalid-feedback').html(data.message);
        }
      },
      error: function(message) {
        alert('failure: ' + message);
      }
    });

  }

});
