import $ from 'jquery';
import Control from 'can-control';

import template from './logs.stache';


export default Control.extend({

  "init": function(element, options) {

    $.get('/logs/' + options.job.attr('id'),function(data) {
      $(element).html(template({
        content: data
      }));
    });
  }
});
