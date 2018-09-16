import Control from 'can-control';
import $ from 'jquery';


import template from './logs.ejs';


export default Control.extend({

  "init": function(element, options) {


    $(element).hide();
    $(element).html(template());
    $(element).fadeIn();
    $("#log-cloudgene").load("/api/v2/admin/server/logs/cloudgene.log");
    $("#log-access").load("/api/v2/admin/server/logs/access.log");
    $("#log-jobs").load("/api/v2/admin/server/logs/jobs.log",
      function(response, status, xhr) {

        if (status == "error") {
          $(element).hide().html(
            can.view('views/error.ejs', {
              error: {
                statusText: xhr.statusText,
                responseText: xhr.responseText
              }
            }));
          $(element).fadeIn();
        }

      });
  }
});
