import can from 'can';
import $ from 'jquery';
import bootbox from 'bootbox';

import ErrorPage from 'helpers/error-page';
import User from 'models/user';
import Group from 'models/group';

import template from './list.ejs';


export default can.Control({

  "init": function(element, options) {
    var that = this;
    User.findAll({
      state: "failed"
    }, function(users) {
      that.element
        .html(template(users));
      $("#content").fadeIn();
    }, function(response) {
      new ErrorPage(that.element, response);
    });

  },

  '.delete-user-btn click': function(el, ev) {

    var user = el.parent().parent().data('user');

    bootbox.confirm("Are you sure you want to delete <b>" + user.attr('username') + "</b>?", function(result) {
      if (result) {
        user.destroy();
      }
    });

  },

  '.edit-role-btn click': function(el, ev) {

    var element = this.element;

    Group.findAll({},
      function(groups) {

        var user = el.parent().parent().data('user');
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
      function(response) {
        new ErrorPage(element, response);
      });

  }

});
