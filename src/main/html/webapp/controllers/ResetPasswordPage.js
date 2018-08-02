// Static Page Controller

ResetPasswordPage = can.Control({

    "init": function(element, options) {
        element.hide();
        element.html(can.view('views/reset-password.ejs'));
        element.fadeIn();
    },

    'submit': function() {

        username = this.element.find("[name='username']");

        $.ajax({
            url: "/api/v2/users/reset",
            type: "POST",
            data: this.element.find("#reset-form").serialize(),
            dataType: 'json',
            success: function(data) {
                if (data.success == true) {

                    // show okey
                    $("#reset-page").hide();
                    $("#success-message").show();
                    $("#success-message").html(data.message);

                } else {
                    // shows error
                    username.addClass('is-invalid');
                    username.closest('.form-group').find('.invalid-feedback').html(data.message);
                }
            },
            error: function(message) {
                alert('failure: ' + message);
            }
        });

        return false;
    }

});
