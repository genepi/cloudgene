// Error Page Controller

ErrorPage = can.Control({

    "init": function(element, options) {
        this.element.hide();

        // check if response
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
        this.element.html(can.view('views/error.ejs', {
            error: error
        }));
        this.element.fadeIn();
    }

});
