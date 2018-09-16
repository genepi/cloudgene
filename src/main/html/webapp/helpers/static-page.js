import $ from 'jquery';
import Control from 'can-control';
import ejs from 'can-ejs';

import ErrorPage from 'helpers/error-page';

export default Control.extend({

  "init": function(element, options) {
    try {

      fetch(options.template).then(function(response) {
        return response.text();
      }).then(function(data) {
        var view = ejs(data);
        if (view) {
          $(element).html(view());
        } else {
          new ErrorPage(element, {
            status: "404",
            message: "Oops, Sorry We Can't Find That Page!"
          });
        }
      });

    } catch (e) {
      new ErrorPage(element, {
        status: "404",
        message: "Oops, Sorry We Can't Find That Page!"
      });
    }
  }

});
