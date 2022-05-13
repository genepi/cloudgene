import Control from 'can-control';
import $ from 'jquery';

import 'components/core/layout/layout.css';
import template from './layout.stache';

export default Control.extend({

  "init": function(element, options) {
    console.log(options);
    var admin = false;
    var appState = options.appState;
    if (appState.attr('loggedIn') && appState.attr('user').attr('admin')){
      admin = true;
    }
    $(element).hide();
    $(element).html(template({
      admin: admin
    }));
    $(element).fadeIn();
  }
});
