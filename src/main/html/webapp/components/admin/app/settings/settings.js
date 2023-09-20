import Control from 'can-control';
import domData from 'can-util/dom/data/data';
import canMap from 'can-map';
import canRoute from 'can-route';

import 'helpers/helpers';
import $ from 'jquery';
import bootbox from 'bootbox';
import showErrorDialog from 'helpers/error-dialog';

import ApplicationSettings from 'models/application-settings';
import template from './settings.stache';

export default Control.extend({

  "init": function (element, options) {
    var that = this;

    ApplicationSettings.findOne({ id: options.app }, function (application) {
      that.application = application;
      $(element).html(template({
        application: application

      }));
      $(element).fadeIn();

    });

  },


  'submit': function (form, event) {
    event.preventDefault();

    var nextflowProfile = $('#nextflow-profile').val();
    var nextflowConfig = $('#nextflow-config').val();
    var nextflowWork = $('#nextflow-work').val();

    this.application.attr('config').attr('nextflow.profile', nextflowProfile);
    this.application.attr('config').attr('nextflow.config', nextflowConfig);
    this.application.attr('config').attr('nextflow.work', nextflowWork);
    this.application.save(function (data) {
      bootbox.alert("Application settings updated.");
    },
      function (response) {
        showErrorDialog("Operation failed", response);
      });
  }


});
