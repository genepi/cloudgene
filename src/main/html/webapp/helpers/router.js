import Control from 'can-control';
import canRoute from 'can-route';
import canMap from 'can-map';
import ajax from 'can-ajax';
import $ from 'jquery';


export default Control.extend({
  defaults: {
    routingMap: new canMap({
      index: 0
    })
  }
}, {

  'init': function(element, options) {
    this.setupAuthentication();
    this.element = $(element);
    canRoute.data = options.routingMap;
    for (var i = 0; i < options.routes.length; i++) {
      var route = options.routes[i];
      canRoute(route.path, {
        index: i
      });
    }

    canRoute.start();
  },

  '{routingMap} change': function() {
    var index = canRoute.data.attr('index');
    var route = this.options.routes[index];
    var data = new canMap();
    data.attr(canRoute.data);
    data.attr(route.options);
    this.activate({
      control: route.control,
      data: data,
      id: '',
      classes: route.classes
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
    if (options.classes) {
      view.addClass(options.classes);
    } else {
      view.addClass(this.options.classes);
    }
    view.html('Loading...');
    this.element.append(view);
    new Control(view[0], data);

  },

  'setupAuthentication': function() {

    $.ajaxPrefilter(function(options) {
      if (!options.beforeSend) {
        options.beforeSend = function(xhr) {
          if (localStorage.getItem("cloudgene")) {
            try {
              // get data
              var data = JSON.parse(localStorage.getItem("cloudgene"));
              xhr.setRequestHeader("X-CSRF-Token", data.csrf);
            } catch (e) {
              // do nothing
            }
          }
        }
      }
    });


  }


});
