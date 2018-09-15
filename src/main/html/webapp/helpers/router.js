import can from 'can/legacy';
import $ from 'jquery';

export default can.Control({
  defaults: {
    routingMap: new can.Map({
      index: 0
    })
  }
}, {
  'init': function(element, options) {
    this.element = $(element);
    can.route.data = options.routingMap;
    for (var i = 0; i < options.routes.length; i++) {
      var route = options.routes[i];
      can.route(route.path, {
        index: i
      });
    }

    can.route.start();
  },

  '{routingMap} change': function() {
    var index = can.route.data.attr('index');
    var data = new can.Map();
    data.attr(can.route.data);
    data.attr(this.options.routes[index].options);
    this.activate({
      control: this.options.routes[index].control,
      data: data,
      id: ''
    });
  },


  activate: function(options) {

    var Control = options.control;
    var data = options.data;
    var id = options.id;

    $(window).scrollTop(0);

    // TODO: activated li --> layout --> navigation
    this.element.find('li').each(function() {
      var li = $(this);
      li.removeClass('active', '');
      $(this).find('a').each(function() {
        if ($(this).attr('id') == id) {
          li.addClass('active');
        }
      });
    });

    this.element.empty();
    var view = $('<div>');
    view.html('Loading...');
    this.element.append(view);
    new Control(view[0], data);

  }

});
