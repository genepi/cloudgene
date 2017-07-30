// controller
AdminSettingsPage = can.Control({

    "init": function(element, options) {
        var that = this;

        Settings.findOne({}, function(settings) {

            that.element.html(can.view('views/admin/settings.ejs', {
                settings: settings
            }));
            that.settings = settings;
            $("#content").fadeIn();

        });

    },

    'submit': function() {

        this.settings.attr('name', this.element.find("[name='name']").val());

        this.settings.attr('hadoopPath', this.element.find("[name='hadoopPath']").val());
        this.settings.attr('userApp', this.element.find("[name='userApp']").val());
        this.settings.attr('adminApp', this.element.find("[name='adminApp']").val());

        this.settings.attr('mail-smtp', this.element.find("[name='mail-smtp']").val());
        this.settings.attr('mail-port', this.element.find("[name='mail-port']").val());
        this.settings.attr('mail-user', this.element.find("[name='mail-user']").val());
        this.settings.attr('mail-password', this.element.find("[name='mail-password']").val());
        this.settings.attr('mail-name', this.element.find("[name='mail-name']").val());
        this.settings.attr('piggene', this.element.find("[name='piggene']").val());

        this.settings.save();

        bootbox.animate(false);
        bootbox.alert("Settings updated.");

        return false;
    }

});
