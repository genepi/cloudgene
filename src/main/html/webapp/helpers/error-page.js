import can from 'can';

export default can.Control({

  "init": function(element, options) {
    this.element.hide();

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
    this.element.html(can.view('helpers/error-page.ejs', {
      error: error
    }));
    this.element.fadeIn();
  }

});
