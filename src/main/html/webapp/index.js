import $ from 'jquery/jquery';
import can from 'can';
import 'popper.js';
import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
import 'assets/css/cloudgene.css';
import '@fortawesome/fontawesome-free/css/all.css';

import PublicRouterControl from 'components/public/';

// open all external link in new tab
$(document.links).filter(function() {
  return this.hostname != window.location.hostname;
}).attr('target', '_blank');


new PublicRouterControl("#content");

can.route.ready();
