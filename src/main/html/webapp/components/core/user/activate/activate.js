import Control from 'can-control';
import $ from 'jquery';

import ErrorPage from 'helpers/error-page';
import template from './activate.ejs';

export default Control.extend({

  "init": function(element, options) {

    $.ajax({
      url: "users/activate/" + options.user + "/" + options.key,
      type: "GET",
      data: $(this).serialize(),
      dataType: 'json',
      success: function(response) {
        $(element).hide();
        $(element).html(template({
          data: response
        }));
        $(element).fadeIn();
      },
      error: function(response) {
        new ErrorPage(element, response);
      }
    });
  }

});
