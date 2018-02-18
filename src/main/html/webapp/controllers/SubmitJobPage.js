// controller
SubmitJobPage = can.Control({

    "init": function(element, options) {
        that = this;
        Application.findOne({
            tool: options.tool
        }, function(application) {
            element.hide();
            element.html(can.view('views/run.ejs', {
                application: application
            }));
            //buildForm(options.tool, "#parameters", details.params, details.submitButton);
            element.fadeIn();

        }, function(message) {
            new ErrorPage(that.element, {
                status: message.statusText,
                message: message.responseText
            });
        });

    },

    '#parameters submit': function(form) {
        try {

						// check required parameters.
            if (form[0].checkValidity() === false) {
                form[0].classList.add('was-validated');
                return false;
            }

            tool = this.element.find('tool');

            $("#waiting-dialog").modal();
            var csrfToken;
            if (localStorage.getItem("cloudgene")) {
                try {

                    // get data
                    var data = JSON.parse(localStorage.getItem("cloudgene"));
                    csrfToken = data.csrf;

                } catch (e) {

                }
            }

            form.ajaxSubmit({
                dataType: 'json',

                headers: {
                    "X-CSRF-Token": csrfToken
                },

                success: function(answer) {
                    if (answer.success) {
                        $("#waiting-dialog").modal('hide');
                        can.route('jobs/:job');
                        can.route.attr({
                            route: 'jobs/:job',
                            job: answer.id,
                            page: 'jobs'
                        });
                    } else {
                        $("#waiting-dialog").modal('hide');
                        new ErrorPage("#content", {
                            status: "",
                            message: answer.message
                        });

                    }
                },

                error: function(message) {
                    $("#waiting-dialog").modal('hide');
                    new ErrorPage("#content", {
                        status: message.statusText,
                        message: message.responseText
                    });

                },

                uploadProgress: function(event, position, total, percentComplete) {
                    $("#waiting-progress").css("width", percentComplete + "%");
                }
            });
        } catch (e) {
            console.log(e);
        }
        return false;
    }

});
