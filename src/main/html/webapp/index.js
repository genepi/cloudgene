import $ from 'jquery';
import 'popper.js';
import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
import '@fortawesome/fontawesome-free/css/all.css';

import Server from 'models/server';
import ErrorPage from 'helpers/error-page';
import LayoutControl from 'components/core/layout/';
import RouterControl from 'helpers/router';
import StaticPage from 'helpers/static-page';
import DashboardControl from 'components/core/dashboard/';
import UserLoginControl from 'components/core/user/login/';
import UserSignupControl from 'components/core/user/signup/';
import UserActivateControl from 'components/core/user/activate/';
import UserPasswordRecoveryControl from 'components/core/user/password-recovery/';
import UserPasswordResetControl from 'components/core/user/password-reset/';
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
  classes: 'fullsize-container'
}, {
  path: 'pages/home',
  control: DashboardControl,
  classes: 'fullsize-container'
}, {
  path: 'pages/contact',
  control: StaticPage,
  options: {
    template: 'static/contact.stache'
  }
}, {
  path: 'pages/help',
  control: StaticPage,
  options: {
    template: 'static/help.stache'
  }
}, {
  path: 'pages/login',
  control: UserLoginControl,
}, {
  path: 'activate/{user}/{key}',
  control: UserActivateControl
}, {
  path: 'pages/recovery/{user}/{key}',
  control: UserPasswordRecoveryControl
}, {
  path: 'pages/register',
  control: UserSignupControl
}, {
  path: 'pages/reset-password',
  control: UserPasswordResetControl
}, {
  path: 'pages/profile',
  control: UserProfileControl,
  guard: loggedInGuard
}, {
  path: 'pages/jobs',
  control: JobListControl,
  options: {
    page: 1
  },
  classes: 'fullsize-container',
  guard: loggedInGuard
}, {
  path: 'pages/jobs/{page}',
  control: JobListControl,
  classes: 'fullsize-container',
  guard: loggedInGuard
}, {
  path: 'jobs/{job}',
  control: JobDetailControl,
  classes: 'fullsize-container',
  guard: loggedInGuard
}, {
  path: 'jobs/{job}/{tab}',
  control: JobDetailControl,
  classes: 'fullsize-container',
  guard: loggedInGuard
}, {
  path: 'run/{app}',
  control: SubmitJobControl,
  classes: 'fullsize-container'
},{
  path: 'pages/{page}',
  control: StaticPage
}];

function loggedInGuard(appState) {
  return appState.attr('loggedIn');
}

Server.findOne({}, function(server) {

  new LayoutControl("#main", {
    server: server
  });

  new RouterControl("#content", {
    routes: routes,
    appState: server,
    classes: 'bg-white bd-content py-5 container bg-white',
    forbidden: {
      control: ErrorPage,
      options: {
        status: '401',
        responseText: 'Oops, you need to <a href="#!pages/login">login</a> to view this content.'
      }
    }
  });

});
