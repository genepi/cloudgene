import Control from 'can-control';
import $ from 'jquery';

import template from './logs.stache';


export default Control.extend({

  "init": function(element, options) {


    $(element).hide();
    $(element).html(template());
    $(element).fadeIn();
    $("#log-cloudgene").load("/api/v2/admin/server/logs/cloudgene.log");
  }
});
