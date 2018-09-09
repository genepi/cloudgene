import can from 'can';
import $ from 'jquery';

import ErrorPage from 'helpers/error-page';
import StaticPage from 'helpers/static-page';
import DashboardControl from 'components/core/dashboard/';
import UserProfileControl from 'components/core/user/profile/';
import JobListControl from 'components/core/job/list/';
import JobDetailControl from 'components/core/job/detail/';
import SubmitJobControl from 'components/core/job/submit/';

export default can.Control({

  'route': function(data) {
    this.activate({
      id: 'home',
      control: DashboardControl,
      options: {
        login: false
      }
    });
  },

  'pages/home route': function(data) {
    this.activate({
      id: 'home',
      control: DashboardControl,
      options: {
        login: false
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

  'pages/profile route': function(data) {
    this.activate({
      id: 'profile',
      control: UserProfileControl,
      options: data
    });
  },

  'pages/jobs route': function(data) {
    data.page2 = 1;
    this.activate({
      id: 'jobs',
      control: JobListControl,
      options: data
    });
  },

  'pages/jobs/:page2 route': function(data) {
    this.activate({
      id: 'jobs',
      control: JobListControl,
      options: data
    });
  },

  'jobs/:job route': function(data) {
    this.activate({
      id: 'jobs',
      control: JobDetailControl,
      options: {
        jobId: data.job,
        admin: false,
        results: false
      }
    });
  },

  'jobs/:job/results route': function(data) {
    this.activate({
      id: 'jobs',
      control: JobDetailControl,
      options: {
        jobId: data.job,
        results: true
      }
    });
  },

  'run/:app route': function(data) {
    this.activate({
      id: 'jobs',
      control: SubmitJobControl,
      options: {
        tool: data.app
      }
    });
  },

  'recovery/:user/:key route': function(data) {
    this.activate({
      id: 'error',
      control: ErrorPage,
      options: {
        status: "276",
        message: "Please log out for user activation."
      }
    });
  },

  'activate/:user/:key route': function(data) {
    this.activate({
      id: 'error',
      control: ErrorPage,
      options: {
        status: "276",
        message: "Please log out for user activation."
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
