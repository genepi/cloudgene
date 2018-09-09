import 'jquery';
import can from 'can';
import 'popper.js';
import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
import 'assets/css/cloudgene.css';
import '@fortawesome/fontawesome-free/css/all.css';

import PublicRouterControl from 'components/public/';

new PublicRouterControl("#content");

can.route.ready();
