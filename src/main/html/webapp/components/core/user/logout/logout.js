import Control from 'can-control';
import $ from 'jquery';

import ErrorPage from 'helpers/error-page';


export default Control.extend({

  "init": function(element, options) {

    localStorage.removeItem("cloudgene");

    var redirect = './';
    window.location = redirect;
  }

});
