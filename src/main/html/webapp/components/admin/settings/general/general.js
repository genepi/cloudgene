import Control from 'can-control';
import $ from 'jquery';
import bootbox from 'bootbox';

import Settings from 'models/settings';

import template from './general.stache';
import showErrorDialog from 'helpers/error-dialog';

export default Control.extend({

  "init": function (element, options) {
    var that = this;

    Settings.findOne({},
      function (settings) {
        $(element).html(template({
          settings: settings
        }));
        that.settings = settings;
        $(element).fadeIn();
      });

  },

  'submit': function (form, event) {
    event.preventDefault();

    this.settings.attr('name', $(form).find("[name='name']").val());
    this.settings.attr('adminName', $(form).find("[name='adminName']").val());
    this.settings.attr('adminMail', $(form).find("[name='adminMail']").val());
    this.settings.attr('serverUrl', $(form).find("[name='serverUrl']").val());
    this.settings.attr('backgroundColor', $(form).find("[name='background-color']").val());
    this.settings.attr('foregroundColor', $(form).find("[name='foreground-color']").val());
    this.settings.attr('googleAnalytics', $(form).find("[name='google-analytics']").val());
    this.settings.attr('workspaceType', $(form).find("[name='workspaceType']").val());
    this.settings.attr('workspaceLocation', $(form).find("[name='workspaceLocation']").val());
    this.settings.save(function (data) {
      bootbox.alert("Settings updated.");
    }, function (response) {
      showErrorDialog("Settings not updated", response);
    });


  }

});
