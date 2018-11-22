import $ from 'jquery';
import Control from 'can-control';
import bootbox from 'bootbox';

import template from './error-page.stache';

export default Control.extend({

  "init": function(element, options) {
    // check if response
    var error = {};
    bootbox.hideAll();

    if (options.responseJSON) {
      error = {
        statusText: options.status,
        responseText: options.responseJSON.message
      };
    } else {
      error = {
        statusText: options.status,
        responseText: options.responseText
      };

    }
    $(element).html(template({
      error: error
    }));
  }

});
