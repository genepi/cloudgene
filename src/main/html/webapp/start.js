import $ from 'jquery';
import 'popper.js';
import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
import 'assets/css/cloudgene.css';
import '@fortawesome/fontawesome-free/css/all.css';

import RouterControl from 'helpers/router';
import ErrorPage from 'helpers/error-page';
import StaticPage from 'helpers/static-page';
import DashboardControl from 'components/core/dashboard/';
import UserProfileControl from 'components/core/user/profile/';
import JobListControl from 'components/core/job/list/';
import JobDetailControl from 'components/core/job/detail/';
import SubmitJobControl from 'components/core/job/submit/';

// open all external link in new tab
$(document.links).filter(function() {
  return this.hostname != window.location.hostname;
}).attr('target', '_blank');

var routes = [{
  path: '',
  control: DashboardControl,
  classes: 'fullsize-container',
  options: {
    login: false
  }
}, {
  path: 'pages/home',
  control: DashboardControl,
  classes: 'fullsize-container',
  options: {
    login: false
  }
}, {
  path: 'pages/contact',
  control: StaticPage,
  options: {
    template: 'static/contact.ejs'
  }
}, {
  path: 'pages/help',
  control: StaticPage,
  options: {
    template: 'static/help.ejs'
  }
}, {
  path: 'pages/profile',
  control: UserProfileControl
}, {
  path: 'pages/jobs',
  control: JobListControl,
  options: {
    page2: 1
  }
}, {
  path: 'pages/jobs/:page2',
  control: JobListControl
}, {
  path: 'jobs/:job',
  control: JobDetailControl,
  options: {
    admin: false,
    results: false
  }
}, {
  path: 'jobs/:job/results',
  control: JobDetailControl,
  options: {
    results: true
  }
}, {
  path: 'run/:app',
  control: SubmitJobControl
}, {
  path: 'recovery/:user/:key',
  control: ErrorPage,
  options: {
    status: "276",
    message: "Please log out for user activation."
  }
}, {
  path: 'activate/:user/:key ',
  control: ErrorPage,
  options: {
    status: "276",
    message: "Please log out for user activation."
  }
}];

new RouterControl("#content", {
  routes: routes,
  classes: 'bd-content py-5 container'
});
