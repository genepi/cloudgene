import can from 'can/legacy';
import $ from 'jquery';
import bootbox from 'bootbox';

import ErrorPage from 'helpers/error-page';
import Cluster from 'models/cluster';
import Template from 'models/template';

import template from './server.ejs';


export default can.Control({

  "init": function(element, options) {
    var that = this;
    $(element).hide();
    Cluster.findOne({}, function(cluster) {
      that.cluster = cluster;
      $(element).html(template({
        cluster: cluster
      }));
      $(element).fadeIn();
    });

  },

  "#maintenance-enter-btn click": function() {
    var that = this;
    var element = this.element;

    Template.findOne({
      key: 'MAINTENANCE_MESSAGE'
    }, function(template) {

      var oldText = template.attr('text');
      bootbox.confirm(
        '<h4>Maintenance Message</h4><form><textarea class="form-control span5" id="message" rows="10" name="message" width="30" height="20">' + oldText + '</textarea></form>',
        function(result) {
          if (result) {
            var text = $('#message').val();
            template.attr('text', text);
            template.save();

            $.get('api/v2/admin/server/maintenance/enter').then(function(data) {
              bootbox.alert(data);
              that.init(element, that.options);
            }, function(data) {
              bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
            });

          }
        });

    }, function(response) {
      new ErrorPage(element, response);
    });

  },

  "#maintenance-exit-btn click": function() {
    var that = this;
    $.get('api/v2/admin/server/maintenance/exit').then(function(data) {
      bootbox.alert(data);
      that.init(that.element, that.options);
    }, function(data) {
      bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
    });
  },

  "#hadoop-details-btn click": function() {
    bootbox.alert('<pre>' + this.cluster.attr('hadoop_details') + '</pre>');
  },

  "#queue-block-btn click": function() {
    var that = this;
    $.get('api/v2/admin/server/queue/block').then(function(data) {
      bootbox.alert(data);
      that.init(that.element, that.options);
    }, function(data) {
      bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
    });
  },

  "#queue-open-btn click": function() {
    var that = this;
    $.get('api/v2/admin/server/queue/open').then(function(data) {
      bootbox.alert(data);
      that.init(that.element, that.options);
    }, function(data) {
      bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
    });
  },

  "#retire-btn click": function() {
    var that = this;
    $.get('api/v2/admin/jobs/retire').then(function(data) {
      bootbox.alert(data);
      that.init(that.element, that.options);
    }, function(data) {
      bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
    });
  }


});
