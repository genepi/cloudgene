// Static Page Controller

StaticPage = can.Control({

    "init": function(element, options) {
        console.log("adsaas");

        element.hide();
        try {

            view = can.view(options.template);
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
