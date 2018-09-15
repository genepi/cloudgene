import $ from 'jquery';
import can from 'can/legacy';

import template from './error-page.ejs';

export default can.Control({



  "init": function(element, options) {
    // check if response
    var error = {};
    if (options.responseJSON) {
      error = {
        statusText: options.status,
        responseText: options.responseJSON.message
      };
    } else {
      error = {
        statusText: options.status,
        responseText: options.message
      };

    }
    $(element).html(template({
      error: error
    }));
  }

});
