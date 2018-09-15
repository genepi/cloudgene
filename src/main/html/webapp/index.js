import 'can/legacy';
import 'popper.js';
import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
import 'assets/css/cloudgene.css';
import '@fortawesome/fontawesome-free/css/all.css';

import RouterControl from 'helpers/router';
import ErrorPage from 'helpers/error-page';
import StaticPage from 'helpers/static-page';
import DashboardControl from 'components/core/dashboard/';
import UserLoginControl from 'components/core/user/login/';
import UserSignupControl from 'components/core/user/signup/';
import UserActivateControl from 'components/core/user/activate/';
import UserPasswordRecoveryControl from 'components/core/user/password-recovery/';
import UserPasswordResetControl from 'components/core/user/password-reset/';

var routes = [{
  path: 'pages/home',
  control: DashboardControl,
  options: {
    login: true
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
  path: 'pages/login',
  control: UserLoginControl,
}, {
  path: 'activate/:user/:key',
  control: UserActivateControl,
}, {
  path: 'pages/recovery/:user/:key',
  control: UserPasswordRecoveryControl,
}, {
  path: 'pages/register',
  control: UserSignupControl,
}, {
  path: 'pages/reset-password',
  control: UserPasswordResetControl,
}, {
  path: 'jobs/:job',
  control: ErrorPage,
  options: {
    status: '404',
    message: 'Oops, Sorry We Can\'t Find That Page!'
  }
}, {
  path: 'jobs/:job/results',
  control: ErrorPage,
  options: {
    status: '404',
    message: 'Oops, Sorry We Can\'t Find That Page!'
  }
}, {
  path: 'run/:app',
  control: ErrorPage,
  options: {
    status: '404',
    message: 'Oops, Sorry We Can\'t Find That Page!'
  }
}];



new RouterControl("#content", {
  routes: routes
});
