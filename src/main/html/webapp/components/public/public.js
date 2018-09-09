import can from 'can';
import $ from 'jquery';

import ErrorPage from 'helpers/error-page';
import StaticPage from 'helpers/static-page';
import DashboardControl from 'components/core/dashboard/';
import UserLoginControl from 'components/core/user/login/';
import UserSignupControl from 'components/core/user/signup/';
import UserActivateControl from 'components/core/user/activate/';
import UserPasswordRecoveryControl from 'components/core/user/password-recovery/';
import UserPasswordResetControl from 'components/core/user/password-reset/';


export default can.Control({

  'route': function(data) {
    this.activate({
      id: 'home',
      control: DashboardControl,
      options: {
        login: true
      }
    });
  },

  'pages/home route': function(data) {
    this.activate({
      id: 'home',
      control: DashboardControl,
      options: {
        login: true
      }
    });
  },

  'pages/contact route': function(data) {
    this.activate({
      id: 'contact',
      control: StaticPage,
      options: {
        template: 'static/contact.ejs'
      }
    });
  },

  'pages/help route': function(data) {
    this.activate({
      id: 'help',
      control: StaticPage,
      options: {
        template: 'static/help.ejs'
      }
    });
  },

  'pages/login route': function(data) {
    this.activate({
      id: 'login',
      control: UserLoginControl,
      options: data
    });
  },

  'activate/:user/:key route': function(data) {
    this.activate({
      id: 'register',
      control: UserActivateControl,
      options: data
    });
  },

  'pages/recovery/:user/:key route': function(data) {
    this.activate({
      id: 'recovery',
      control: UserPasswordRecoveryControl,
      options: data
    });
  },

  'pages/register route': function(data) {
    this.activate({
      id: 'signup',
      control: UserSignupControl,
      options: data
    });
  },

  'pages/reset-password route': function(data) {
    this.activate({
      id: 'recovery',
      control: UserPasswordResetControl,
      options: data
    });
  },

  'jobs/:job route': function(data) {
    this.activate({
      id: 'error',
      control: ErrorPage,
      options: {
        status: '401',
        message: 'The request requires user authentication.'
      }
    });
  },

  'jobs/:job/results route': function(data) {
    this.activate({
      id: 'error',
      control: ErrorPage,
      options: {
        status: '401',
        message: 'The request requires user authentication.'
      }
    });
  },

  'run/:app route': function(data) {
    this.activate({
      id: 'error',
      control: ErrorPage,
      options: {
        status: '401',
        message: 'The request requires user authentication.'
      }
    });
  },

  'pages/:page route': function(data) {
    var id = data.page;
    var template = 'static/' + id + '.ejs';
    this.activate({
      id: id,
      control: ErrorPage,
      options: {
        template: template
      }
    });
  },

  '.* route': function(data) {
    this.activate({
      id: 'error',
      control: ErrorPage,
      options: {
        status: '404',
        message: 'Oops, Sorry We Can\'t Find That Page!'
      }
    });
  },

  activate: function(options) {

    var Control = options.control;
    var data = options.options;
    var id = options.id;

    $(window).scrollTop(0);

    // TODO: activated li --> layout --> navigation
    this.element.find('li').each(function() {
      var li = $(this);
      li.removeClass('active', '');
      $(this).find('a').each(function() {
        if ($(this).attr('id') == id) {
          li.addClass('active');
        }
      });
    });

    this.element.empty();
    var view = $('<div>');
    this.element.append(view);
    new Control(view, data);

  }

});
