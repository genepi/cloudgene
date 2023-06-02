import Control from 'can-control';
import deparam from 'can-deparam';
import $ from 'jquery';
import bootbox from 'bootbox';

import ErrorPage from 'helpers/error-page';
import User from 'models/user';
import Template from 'models/template';
import UserToken from 'models/user-token';
import UserProfile from 'models/user-profile';

import template from './profile.stache';
import templateDeleteDialog from './dialogs/delete.stache';
import templateNewTokenDialog from './dialogs/new.stache'

export default Control.extend({

  "init": function(element, options) {

    $(element).hide();
    User.findOne({
      user: 'me'
    }, function(user) {
      $(element).html(template({
        user: user
      }));
      options.user = user;
      $(element).fadeIn();
    });
  },

  'submit': function(element, event) {
    event.preventDefault();
    var user = new User();

    // fullname
    var fullname = $(element).find("[name='full-name']");
    var fullnameError = user.checkName(fullname.val());
    this.updateControl(fullname, fullnameError);

    // mail
    var mail = $(element).find("[name='mail']");
    var mailError = user.checkMail(mail.val());
    this.updateControl(mail, mailError);

    // password if password is not empty. else no password update on server side
    var newPassword = $(element).find("[name='new-password']");
    var newPasswordError = undefined;
    if (newPassword.val() !== "") {
      var confirmNewPassword = $(element).find("[name='confirm-new-password']");
      newPasswordError = user.checkPassword(newPassword.val(), confirmNewPassword.val());
      this.updateControl(confirmNewPassword, newPasswordError);
    }
    if (fullnameError || mailError || newPasswordError) {
      return false;
    }

    $.ajax({
      url: "/api/v2/users/me/profile",
      type: "POST",
      data: $(element).find("#account-form").serialize(),
      dataType: 'json',
      success: function(data) {

        if (data.success == true) {

          // shows okey
          bootbox.alert(data.message);

        } else {
          // shows error
          bootbox.alert(data.message);

        }
      },
      error: function(response) {
        new ErrorPage(element, response);
      }
    });

  },

  '#create_token click': function() {

    //load template
    var that = this;
    Template.findOne({
      key: 'TERMS'
    }, function(template) {

      bootbox.confirm({
        message: '<h4>Terms of Service</h4>' + template.attr('text'),
        buttons: {
          confirm: {
            label: 'I Agree',
            className: 'btn-success'
          }
        },
        callback: function(result) {
          if (result) {

            var token_expiration = $('#token_expiration').val();

            var user = that.options.user;

            var userToken = new UserToken();
            userToken.attr('user', user.attr('username'));
            userToken.attr('expiration', token_expiration);

            userToken.save(function(responseText) {
              user.attr('hasApiToken', true);
              user.attr('apiTokenValid', true);
              user.attr('apiTokenMessage', "");

              bootbox.alert({
                message: templateNewTokenDialog({
                  token: responseText.token
                })
              });

            }, function(message) {
              bootbox.alert('<h4>API Token</h4>Error: ' + message);
            });
          }
        }
      })
    });
  },

  '#revoke_token click': function() {

    var user = this.options.user;

    bootbox.confirm("Are you sure you want to revoke your <b>API Token</b>? All your applications and scripts that are you using this API token have to be changed!", function(result) {

      if (result) {
        var userToken = new UserToken();
        userToken.attr('user', user.attr('username'));
        userToken.attr('id', 'luki');
        userToken.destroy(function() {
          user.attr('hasApiToken', false);
          user.attr('apiTokenValid', true);
          user.attr('apiTokenMessage', "");
          bootbox.alert('<h4>API Token</h4>Your token is now inactive.');
        }, function(response) {
          bootbox.alert('<h4>API Token</h4>Error: ' + response);
        });
      }
    });
  },

  '#show_token click': function() {
    var user = this.options.user;

    UserToken.findOne({
      user: user.attr('username')
    }, function(responseText) {
      user.attr('hasApiToken', true);
      bootbox.alert('<h4>API Token</h4>Your token for this service is:<br><textarea style="width:100%;height:100px;">' + responseText.token + '</textarea>');
    }, function(message) {
      bootbox.alert('<h4>API Token</h4>Error: ' + message);
    });

  },

  updateControl: function(control, error) {
    if (error) {
      control.removeClass('is-valid');
      control.addClass('is-invalid');
      control.closest('.form-group').find('.invalid-feedback').html(error);
    } else {
      control.removeClass('is-invalid');
      control.addClass('is-valid');
      control.closest('.form-group').find('.invalid-feedback').html('');
    }
  },

  '#delete_account click': function() {


    var deleteAcountDialog = bootbox.dialog({
           message: templateDeleteDialog(),
           buttons: {
            cancel: {
              label: "Cancel",
              class: "btn-default",
              callback: function() {}
            },
            ok: {
               label: "Delete Account",
               class: "btn-danger",
               callback: function() {

                 // get form parameters
                 var form = deleteAcountDialog.find("form");
                 var values = deparam(form.serialize());

                 // create delete request
                 var userProfile = new UserProfile();
                 userProfile.attr('user', values['username']);
                 userProfile.attr('username', values['username']);
                 userProfile.attr('password', values['password']);
                 userProfile.attr('id', 'id');
                 userProfile.destroy(function() {
                   bootbox.alert('<h4>Account deleted</h4>Your account is now deleted.');
                   window.location.href = 'logout';
                   return true;
                 }, function(message) {
                   var response = JSON.parse(message.responseText);
                   bootbox.alert('<h4>Account not deleted</h4>Error: ' + response.message);
                   return false;
                 });
               }
             }
          }
       });


  }

});