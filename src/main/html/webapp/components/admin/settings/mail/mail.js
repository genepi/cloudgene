import can from 'can';
import $ from 'jquery';
import bootbox from 'bootbox';

import Settings from 'models/settings';

import template from './mail.ejs';


export default can.Control({

  "init": function(element, options) {
    var that = this;

    Settings.findOne({}, function(settings) {

      that.element.html(template({
        settings: settings
      }));
      that.settings = settings;
      $("#content").fadeIn();

    });

  },

  '#mail change': function(e) {
    if (e[0].checked) {
      this.settings.attr('mail', "true");
    } else {
      this.settings.attr('mail', "false");
    }
  },

  'submit': function() {

    this.settings.attr('mail-smtp', this.element.find("[name='mail-smtp']").val());
    this.settings.attr('mail-port', this.element.find("[name='mail-port']").val());
    this.settings.attr('mail-user', this.element.find("[name='mail-user']").val());
    this.settings.attr('mail-password', this.element.find("[name='mail-password']").val());
    this.settings.attr('mail-name', this.element.find("[name='mail-name']").val());
    this.settings.attr('piggene', this.element.find("[name='piggene']").val());

    this.settings.save();

    bootbox.alert("E-Mail configuration updated.");

    return false;
  }

});
