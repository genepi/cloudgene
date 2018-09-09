// controller
AdminSettingsTemplatesPage = can.Control({

    "init": function(element, options) {
        var that = this;
        Template.findAll({}, function(templates) {

            that.element.html(can.view('views/admin/settings.templates.ejs', {
                templates: templates,
            }));
            $("#content").fadeIn();

        }, function(response) {
          new ErrorPage(that.element, response);
        });
    },

    '.edit-btn click': function(el, ev) {

        template = el.parent().parent().data('template');

        var oldText = template.attr('text');
        bootbox.confirm(
            '<h4>' + template.attr('key') + '</h4><form><textarea class="form-control span5" id="message" rows="10" name="message" width="30" height="20">' + oldText + '</textarea></form>',
            function(result) {
                if (result) {
                    var text = $('#message').val();
                    template.attr('text', text);
                    template.save();
                }
            });

    }

});
