import Control from 'can-control';
import $ from 'jquery';
import bootbox from 'bootbox';

import NextflowConfig from 'models/nextflow-config';

import template from './nextflow.stache';
import showErrorDialog from 'helpers/error-dialog';

export default Control.extend({

  "init": function (element, options) {
    var that = this;

    NextflowConfig.findOne({},
      function (nextflowConfig) {
        $(element).html(template({
          nextflowConfig: nextflowConfig
        }));
        that.nextflowConfig = nextflowConfig;
        $(element).fadeIn();
      });

  },

  'submit': function (form, event) {
    event.preventDefault();

    this.nextflowConfig.attr('content', $(form).find("[name='content']").val());
    this.nextflowConfig.save(function (data) {
      bootbox.alert("Nextflow configuration updated.");
    }, function (response) {
      showErrorDialog("Nextflow configuration not updated", response);
    });


  }

});
