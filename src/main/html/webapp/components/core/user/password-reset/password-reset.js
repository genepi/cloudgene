import can from 'can';
import $ from 'jquery';

import template from './password-reset.ejs';


export default can.Control({

  "init": function(element, options) {
    element.hide();
    element.html(template());
    element.fadeIn();
  },

  'submit': function() {

    var username = this.element.find("[name='username']");

    $.ajax({
      url: "/api/v2/users/reset",
      type: "POST",
      data: this.element.find("#reset-form").serialize(),
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

    return false;
  }

});
