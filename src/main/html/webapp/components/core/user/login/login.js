import can from 'can';
import $ from 'jquery';

import ErrorPage from 'helpers/error-page';
import template from './login.ejs';


export default can.Control({

  "init": function(element, options) {
    element.hide();
    element.html(template());
    element.fadeIn();
  },

  'submit': function() {

    var element = this.element;

    var password = element.find("[name='loginPassword']");

    $.ajax({
      url: "login",
      type: "POST",
      data: this.element.find("#signin-form").serialize(),
      dataType: 'json',
      success: function(response) {
        if (response.success == true) {

          //save CSRF token to local storage
          var dataToken = {
            csrf: response.csrf
          };
          localStorage.setItem('cloudgene', JSON.stringify(dataToken));

          var redirect = 'start.html#!run/';
          window.location = redirect;

        } else {
          // shows error
          var message = response.message;
          password.addClass('is-invalid');
          password.closest('.form-group').find('.invalid-feedback').html(message);
        }
      },
      error: function(response) {
        new ErrorPage(element, response);
      }
    });

    return false;
  }

});
