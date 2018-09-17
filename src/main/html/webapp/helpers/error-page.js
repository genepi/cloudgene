import $ from 'jquery';
import Control from 'can-control';

import template from './error-page.stache';

export default Control.extend({

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
