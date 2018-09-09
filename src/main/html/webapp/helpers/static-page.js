import can from 'can';

import ErrorPage from 'helpers/error-page';

export default can.Control({

  "init": function(element, options) {

    element.hide();
    try {

      var view = can.view(options.template);
      if (view) {
        element.html(view);
        element.fadeIn();
      } else {
        new ErrorPage(element, {
          status: "404",
          message: "Oops, Sorry We Can't Find That Page!"
        });
      }

    } catch (e) {
      new ErrorPage(element, {
        status: "404",
        message: "Oops, Sorry We Can't Find That Page!"
      });
    }
  }

});
