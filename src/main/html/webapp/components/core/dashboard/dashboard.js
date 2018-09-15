import $ from 'jquery';
import can from 'can/legacy';

import Counter from 'models/counter';

import template from 'static/home.ejs';


export default can.Control({

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
