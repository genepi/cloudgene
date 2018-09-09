import can from 'can';

import Counter from 'models/counter';
import UserLoginControl from 'components/core/user/login/'

import template from 'static/home.ejs';


export default can.Control({

  "init": function(element, options) {

    var that = this;

    Counter.findOne({}, function(counter) {
      that.element.hide();
      that.element.html(template({
        counter: counter,
        loggedIn: !options.login
      }));
      that.element.fadeIn();
      if (options.login) {
        new UserLoginControl("#login-form");
      }
    }, function(message) {

    });
  }

});
