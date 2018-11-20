import Control from 'can-control';
import $ from 'jquery';

import 'components/core/layout/layout.css';
import template from './layout.stache';

export default Control.extend({

  "init": function(element, options) {
    $(element).hide();
    $(element).html(template());
    $(element).fadeIn();
  }
});
