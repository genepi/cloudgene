// controller
AdminAppsPage = can.Control({

    "init": function(element, options) {
        var that = this;

        Application.findAll({}, function(applications) {

            that.element.html(can.view('views/admin/apps.ejs', {
                applications: applications
            }));
            $("#content").fadeIn();

        });

    },

    '#install-app-btn click': function(el, ev) {
        bootbox.animate(false);
        bootbox.confirm(
            '<h4>Install App from URL</h4><p>Please enter the URL of the zip file of the application.</p><form><input class="field span5" id="url" name="url" value="http://"/></form>',
            function(result) {
                if (result) {
                    var url = $('#url').val();
                    app = new Application();
                    app.attr('url', url);
                    app.save();
                }
            });

    },

    '.icon-trash click': function(el, ev) {

        application = el.closest('tr').data('application');
        bootbox.animate(false);
        bootbox.confirm("Are you sure you want to delete <b>" + application.attr('id') + "</b>?", function(result) {
            if (result) {
                application.destroy();
            }
        });

    },

});
