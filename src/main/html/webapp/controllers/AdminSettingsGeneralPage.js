// controller
AdminSettingsGeneralPage = can.Control({

    "init": function(element, options) {
        var that = this;

        Settings.findOne({}, function(settings) {

            that.element.html(can.view('views/admin/settings.general.ejs', {
                settings: settings
            }));
            that.settings = settings;
            $("#content").fadeIn();

        });

    },

    'submit': function() {

        this.settings.attr('name', this.element.find("[name='name']").val());
        this.settings.attr('background-color', this.element.find("[name='background-color']").val());
        this.settings.attr('foreground-color', this.element.find("[name='foreground-color']").val());
        this.settings.attr('google-analytics', this.element.find("[name='google-analytics']").val());

        this.settings.save();


        bootbox.alert("Settings updated.");

        return false;
    }

});
