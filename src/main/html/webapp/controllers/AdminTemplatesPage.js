// controller
AdminTemplatesPage = can.Control({

    "init": function(element, options) {
        var that = this;
        Template.findAll({}, function(templates) {

            that.element.html(can.view('views/admin/templates.ejs', {
                templates: templates,
            }));
            $("#content").fadeIn();

        }, function(message) {
            new ErrorPage(that.element, {
                status: message.statusText,
                message: message.responseText
            });
        });
    },

    '.icon-pencil click': function(el, ev) {

        template = el.parent().parent().data('template');
        bootbox.animate(false);
        var oldText = template.attr('text');
        bootbox.confirm(
            '<h4>' + template.attr('key') + '</h4><form><textarea class="field span5" id="message" rows="10" name="message" width="30" height="20">' + oldText + '</textarea></form>',
            function(result) {
                if (result) {
                    var text = $('#message').val();
                    template.attr('text', text);
                    template.save();
                }
            });

    }

});
