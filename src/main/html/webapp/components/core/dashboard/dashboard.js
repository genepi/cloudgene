import $ from 'jquery';
import Control from 'can-control';
import stache from 'can-stache';

import Counter from 'models/counter';

export default Control.extend({

  "init": function(element, options) {
    var url = 'static/home.stache';
    $.get(url,
      function(data) {

        var template = stache(data);

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

      });
  }
});
