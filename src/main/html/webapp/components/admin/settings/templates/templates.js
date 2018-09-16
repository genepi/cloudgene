import Control from 'can-control';
import domData from 'can-util/dom/data/data';
import $ from 'jquery';
import bootbox from 'bootbox';

import ErrorPage from 'helpers/error-page';
import Template from 'models/template';

import template from './templates.ejs';


export default Control.extend({

  "init": function(element, options) {
    Template.findAll({},
      function(templates) {
        $(element).html(template({
          templates: templates,
        }));
        $("#content").fadeIn();
      },
      function(response) {
        new ErrorPage(element, response);
      });
  },

  '.edit-btn click': function(el, ev) {

    var tr = $(el).closest('tr');
    var template = domData.get.call(tr[0], 'template');
    var oldText = template.attr('text');
    bootbox.confirm(
      '<h4>' + template.attr('key') + '</h4><form><textarea class="form-control span5" id="message" rows="10" name="message" width="30" height="20">' + oldText + '</textarea></form>',
      function(result) {
        if (result) {
          var text = $('#message').val();
          template.attr('text', text);
          template.save();
        }
      });

  }

});
