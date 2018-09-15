import $ from 'jquery';
import can from 'can/legacy';

import ErrorPage from 'helpers/error-page';

export default can.Control({

  "init": function(element, options) {
    try {

      fetch(options.template).then(function(response) {
        return response.text();
      }).then(function(data) {
        var view = can.EJS(data);
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
