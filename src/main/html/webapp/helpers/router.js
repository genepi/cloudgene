import Control from 'can-control';
import canRoute from 'can-route';
import canMap from 'can-map';
import 'can-ajax';
import $ from 'jquery';


export default Control.extend({
  defaults: {
    routingMap: new canMap({}),
    lastBatchNum: undefined,
    appState: new canMap(),
    timeoutMin: 60
  }
}, {

  'init': function(element, options) {
    this.setupAuthentication();

    this.element = $(element);
    canRoute.data = options.routingMap;
    options.routesByPath = {};
    for (var i = 0; i < options.routes.length; i++) {
      var route = options.routes[i];
      canRoute(route.path, {
        path: route.path
      });
      options.routesByPath[route.path] = route;
    }

    canRoute.bind('change', this.check)
    canRoute.router = this;
    canRoute.start();
  },

  'check': function(ev, attr, how) {
    var router = canRoute.router;
    if ((router.lastBatchNum == undefined || router.lastBatchNum != ev.batchNum) && (canRoute.data.attr('path') != undefined && canRoute.data.attr('path') == canRoute.matched())) {
      router.lastBatchNum = ev.batchNum;
      var path = canRoute.data.attr('path');
      console.log('[Router] Found control for ' + path);
      var route = router.options.routesByPath[path];
      var control = route.control;
      console.log('[Router] Parameters: ');
      console.log(canRoute.data.attr());
      var data = new canMap();
      data.attr(canRoute.data);
      data.attr(route.options);
      data.attr('appState', router.options.appState);
      if (route.guard) {
        var allowed = route.guard(router.options.appState);
        if (allowed == false) {
          control = router.options.forbidden.control;
          data.attr(router.options.forbidden.options);
        }
      }
      router.activeControl = {
        control: control,
        data: data,
        id: '',
        classes: route.classes
      };
      router.activate(router.activeControl);
    }
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
    view.html('');
    this.element.append(view);
    new Control(view[0], data);

  },

  'reload': function() {
    var router = canRoute.router;
    if (router.activeControl) {
      router.activate(router.activeControl);
    }
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
