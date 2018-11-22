import Control from 'can-control';
import domData from 'can-util/dom/data/data';
import $ from 'jquery';
import md5 from 'md5';
import bootbox from 'bootbox';

import ErrorPage from 'helpers/error-page';
import User from 'models/user';
import Group from 'models/group';

import template from './list.stache';
import showErrorDialog from 'helpers/error-dialog';


export default Control.extend({

  "init": function(element, options) {

    var params = {};
    if (options.query) {
      params = {
        query: options.query
      }
    } else {
      params = {
        page: options.page
      }
    }

    User.findAll(
      params,
      function(users) {
        $(element).html(template({
          users: users,
          md5: md5,
          query: options.query
        }));
        $(element).fadeIn();
      },
      function(response) {
        new ErrorPage(element, response);
      });

  },

  '.delete-user-btn click': function(el, ev) {
    var tr = $(el).closest('tr');
    var user = domData.get.call(tr[0], 'user');

    bootbox.confirm("Are you sure you want to delete <b>" + user.attr('username') + "</b>?", function(result) {
      if (result) {
        user.destroy(function(data) {}, function(response) {
          showErrorDialog("User not deleted", response);
        });
      }
    });

  },

  '.edit-role-btn click': function(el, ev) {
    var tr = $(el).closest('tr');
    var user = domData.get.call(tr[0], 'user');
    var element = this.element;

    Group.findAll({},
      function(groups) {

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
          options = options + ' <b>' + group.attr('name') + '</b><br><small class="text-muted">Access to: ' + group.attr('apps').join(', ') + '</small></label>';
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
              user.save(function(data) {

                },
                function(response) {
                  showErrorDialog("User not deleted", response);
                });
            }
          }
        );

      },
      function(response) {
        new ErrorPage(element, response);
      });

  },

  'submit': function(el, ev) {

    event.preventDefault();

    var query = $(this.element).find('#query');
    if (query.val() != '') {
      window.location.href = "#!pages/users/search/" + query.val();
    } else {
      window.location.href = "#!pages/users";
    }

  },

});
