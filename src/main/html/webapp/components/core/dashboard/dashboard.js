import $ from 'jquery';
import Control from 'can-control';

import Counter from 'models/counter';

import template from 'static/home.stache';


export default Control.extend({

  "init": function(element, options) {

    Counter.findOne({}, function(counter) {
      $(element).html(template({
        counter: counter,
        loggedIn: options.appState.loggedIn
      }));
    }, function(message) {
      $(element).html(template({
        counter: undefined,
        loggedIn: options.loggedIn
      }));
    });
  }

});
