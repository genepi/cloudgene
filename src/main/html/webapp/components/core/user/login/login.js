import Control from 'can-control';
import $ from 'jquery';

import ErrorPage from 'helpers/error-page';
import template from './login.stache';


export default Control.extend({

  "init": function(element, options) {
    $(element).hide();
    $(element).html(template({
      oauth: options.appState.attr('oauth')
    }));
    $(element).fadeIn();
  },

  'submit': function(element, event) {
    event.preventDefault();

    var password = $(element).find("[name='password']");

    $.ajax({
      url: "/login",
      type: "POST",
      data: $(element).find("#signin-form").serialize(),
      dataType: 'json',
      success: function(response) {

          var dataToken = {
            csrf: response.csrf,
            token: response.access_token
          };
          localStorage.setItem('cloudgene', JSON.stringify(dataToken));

          var redirect = '/';
          window.location = redirect;

      },
      error: function(response) {
        console.log(response);
        password.addClass('is-invalid');
        password.closest('.form-group').find('.invalid-feedback').html(response.responseJSON.message);

      }
    });

  }

});
