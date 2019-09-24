import $ from 'jquery';
import Control from 'can-control';
import domData from 'can-util/dom/data/data';
import bootbox from 'bootbox';

import template from './results.stache';
import templateShareFolder from './share-folder.stache';
import templateShareFile from './share-file.stache';
import './results.css';


export default Control.extend({

  "init": function(element, options) {
    $(element).html(template({
      job: options.job
    }));
  },

  '.share-file-btn click': function(el) {
    var tr = $(el).closest('div');
    var output = domData.get.call(tr[0], 'output');
    bootbox.alert(templateShareFile({
      hostname: location.protocol + '//' + location.host,
      output: output
    }), function() {

    });
  },

  '.share-folder-btn click': function(el) {
    var tr = $(el).closest('div');
    var param = domData.get.call(tr[0], 'param');
    var files = param.attr('files');
    bootbox.alert(templateShareFolder({
      hostname: location.protocol + '//' + location.host,
      files: files
    }), function() {

    });
  },

  // file tree (collapsible folders)

  '.folder-item click': function(el) {
    var ul = $(el).parent().children('UL');
    ul.slideToggle();
    if ($(el).hasClass('fa-angle-down')) {
      $(el).addClass('fa-angle-right ');
      $(el).removeClass('fa-angle-down');
    } else {
      $(el).addClass('fa-angle-down');
      $(el).removeClass('fa-angle-right');
    }
  },

  '.folder-item-text click': function(el) {
    var item = $(el).parent().children('i')
    var ul = $(el).parent().children('UL');
    ul.slideToggle();
    if ($(item).hasClass('fa-angle-down')) {
      $(item).addClass('fa-angle-right ');
      $(item).removeClass('fa-angle-down');
    } else {
      $(item).addClass('fa-angle-down');
      $(item).removeClass('fa-angle-right');
    }
  }

});
