import $ from 'jquery';
import Control from 'can-control';
import domData from 'can-util/dom/data/data';
import bootbox from 'bootbox';

import template from './results.stache';
import templateShareFolder from './share-folder.stache';
import templateShareFile from './share-file.stache';


export default Control.extend({

  "init": function(element, options) {
    $(element).html(template({
      job: options.job
    }));
  },

  '.share-file-btn click': function(el) {
    var tr = $(el).closest('tr');
    var output = domData.get.call(tr[0], 'output');
    bootbox.alert(templateShareFile({
      hostname: location.protocol + '//' + location.host,
      output: output
    }), function() {

    });
  },

  '.share-folder-btn click': function(el) {
    var tr = $(el).closest('tr');
    var param = domData.get.call(tr[0], 'param');
    var files = param.attr('files');
    bootbox.alert(templateShareFolder({
      hostname: location.protocol + '//' + location.host,
      files: files
    }), function() {

    });
  }

});
