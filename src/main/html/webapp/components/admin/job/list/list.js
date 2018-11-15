import 'can-map-define';
import Control from 'can-control';
import $ from 'jquery';
import 'helpers/helpers';

import 'helpers/helpers';

import JobTable from './table/';

import template from './list.stache';

export default Control.extend({

  "init": function(element, options) {
    $(element).html(template());
    $(element).fadeIn();

    new JobTable("#job-list-running-stq", {state: "running-stq"});
    new JobTable("#job-list-running-ltq", {state: "running-ltq"});
    new JobTable("#job-list-current", {state: "current"});

  }

});
