import Control from 'can-control';
import $ from 'jquery';
import bootbox from 'bootbox';

import Settings from 'models/settings';

import template from './mail.ejs';


export default Control.extend({

  "init": function(element, options) {
    var that = this;

    Settings.findOne({},
      function(settings) {
        $(element).html(template({
          settings: settings
        }));
        that.settings = settings;
        $(element).fadeIn();
      });

  },

  '#mail change': function(e) {
    if (e.checked) {
      this.settings.attr('mail', "true");
    } else {
      this.settings.attr('mail', "false");
    }
  },

  'submit': function(form, event) {
    event.preventDefault();

    this.settings.attr('mail-smtp', $(form).find("[name='mail-smtp']").val());
    this.settings.attr('mail-port', $(form).find("[name='mail-port']").val());
    this.settings.attr('mail-user', $(form).find("[name='mail-user']").val());
    this.settings.attr('mail-password', $(form).find("[name='mail-password']").val());
    this.settings.attr('mail-name', $(form).find("[name='mail-name']").val());
    this.settings.attr('piggene', $(form).find("[name='piggene']").val());
    this.settings.save();

    bootbox.alert("E-Mail configuration updated.");

  }

});
