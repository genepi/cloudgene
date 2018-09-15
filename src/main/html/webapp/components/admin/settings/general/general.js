import can from 'can/legacy';
import $ from 'jquery';
import bootbox from 'bootbox';

import Settings from 'models/settings';

import template from './general.ejs';


export default can.Control({

  "init": function(element, options) {
    var that = this;

    Settings.findOne({}, function(settings) {

      $(element).html(template({
        settings: settings
      }));
      that.settings = settings;
      $(element).fadeIn();

    });

  },

  'submit': function(element, event) {
    event.preventDefault();

    this.settings.attr('name', this.element.find("[name='name']").val());
    this.settings.attr('background-color', this.element.find("[name='background-color']").val());
    this.settings.attr('foreground-color', this.element.find("[name='foreground-color']").val());
    this.settings.attr('google-analytics', this.element.find("[name='google-analytics']").val());

    this.settings.save();


    bootbox.alert("Settings updated.");
  }

});
