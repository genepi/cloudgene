// controller
AdminServerPage = can.Control({

  "init": function(element, options) {
    that = this;
    element.hide();
    Cluster.findOne({}, function(cluster) {
      that.cluster = cluster;
      element.html(can.view('views/admin/server.ejs', {
        cluster: cluster
      }));
      element.fadeIn();
    });

  },

  "#maintenance-enter-btn click": function() {
    that = this;

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

            request = $.get('api/v2/admin/server/maintenance/enter').then(function(data) {
              bootbox.alert(data);
              that.init(that.element, that.options);
            }, function(data) {
              bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
            });

          }
        });

    }, function(response) {
      new ErrorPage(that.element, response);
    });

  },

  "#maintenance-exit-btn click": function() {
    that = this;
    request = $.get('api/v2/admin/server/maintenance/exit').then(function(data) {
      bootbox.alert(data);
      that.init(that.element, that.options);
    }, function(data) {
      bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
    });
  },

  "#hadoop-details-btn click": function(){
      bootbox.alert('<pre>'+ this.cluster.attr('hadoop_details')+'</pre>');
  },

  "#queue-block-btn click": function() {
    that = this;
    request = $.get('api/v2/admin/server/queue/block').then(function(data) {
      bootbox.alert(data);
      that.init(that.element, that.options);
    }, function(data) {
      bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
    });
  },

  "#queue-open-btn click": function() {
    that = this;
    request = $.get('api/v2/admin/server/queue/open').then(function(data) {
      bootbox.alert(data);
      that.init(that.element, that.options);
    }, function(data) {
      bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
    });
  },

  "#retire-btn click": function() {
    that = this;
    request = $.get('api/v2/admin/jobs/retire').then(function(data) {
      bootbox.alert(data);
      that.init(that.element, that.options);
    }, function(data) {
      bootbox.alert('<p class="text-danger">Operation failed.</p>' + data.responseText);
    });
  }


});
