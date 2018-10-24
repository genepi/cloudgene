import $ from 'jquery';
import Control from 'can-control';

import template from './steps.stache';


export default Control.extend({

  "init": function(element, options) {
    $(element).html(template({
      job: options.job
    }));
  }
});
