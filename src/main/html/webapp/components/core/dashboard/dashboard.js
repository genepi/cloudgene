import $ from 'jquery';
import Control from 'can-control';

import Counter from 'models/counter';

import template from 'static/home.ejs';


export default Control.extend({

  "init": function(element, options) {

    Counter.findOne({}, function(counter) {
      $(element).html(template({
        counter: counter,
        loggedIn: !options.login
      }));
    }, function(message) {
      $(element).html(template({
        counter: undefined,
        loggedIn: !options.login
      }));
    });
  }

});
