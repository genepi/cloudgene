import $ from 'jquery';
import can from 'can';
import 'popper.js';
import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
import 'assets/css/cloudgene.css';
import '@fortawesome/fontawesome-free/css/all.css';

import RouterControl from 'components/core/';


$(document.links).filter(function() {
  return this.hostname != window.location.hostname;
}).attr('target', '_blank');
// add token to every ajax request
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

new RouterControl("#content");
can.route.ready();
