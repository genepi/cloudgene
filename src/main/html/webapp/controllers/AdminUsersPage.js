// controller
AdminUsersPage = can
    .Control({

        "init": function(element, options) {
            var that = this;
            User.findAll({
                state: "failed"
            }, function(users) {
                that.element
                    .html(can.view('views/admin/users.ejs', users));
                $("#content").fadeIn();
            }, function(message) {
                new ErrorPage(that.element, {
                    status: message.statusText,
                    message: message.responseText
                });
            });

        },

        '.icon-trash click': function(el, ev) {

            user = el.parent().parent().data('user');
            bootbox.animate(false);
            bootbox.confirm("Are you sure you want to delete <b>" + user.attr('username') + "</b>?", function(result) {
                if (result) {
                    user.destroy();
                }
            });

        },

        '.icon-pencil click': function(el, ev) {

            Group.findAll({},
                function(groups) {

                    user = el.parent().parent().data('user');
                    var roles = user.attr('role').split(',');

                    var options = '';
                    groups.forEach(function(group, index) {
                        if ($.inArray(group.attr('name'), roles) >= 0) {
                            options = options + '<label class="checkbox"><input type="checkbox" name="role-select" value="' + group.attr('name') + '" checked />';
                            //options = options + '<option selected>' + group.attr('name') + '</option>';
                        } else {
                            //options = options + '<option>' + group.attr('name') + '</option>';
                            options = options + '<label class="checkbox"><input type="checkbox" name="role-select" value="' + group.attr('name') + '" />';
                        }
                        options = options + '<b>' + group.attr('name') + '</b><br>Access to: ' + group.attr('apps').join(', ') + '</label>';
                    });
                    bootbox.animate(false);
                    bootbox.confirm(
                        '<h4>Edit roles of user ' + user.attr('username') + '</h4><hr><form id="role-form">' + options + '</form>',
                        function(result) {
                            if (result) {

                                var boxes = $('#role-form input:checkbox');
                                var checked = [];
                                for (var i = 0; boxes[i]; ++i) {
                                    if (boxes[i].checked) {
                                        checked.push(boxes[i].value);
                                    }
                                }

                                var text = checked.join(',');
                                user.attr('role',
                                    text);
                                user.save();
                            }
                        }
                    );

                },
                function(message) {
                    new ErrorPage(that.element, {
                        status: message.statusText,
                        message: message.responseText
                    });
                });

        }

    });
