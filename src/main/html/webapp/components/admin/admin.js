import $ from 'jquery';
import can from 'can';

import ErrorPage from 'helpers/error-page';
import DashboardControl from 'components/admin/dashboard/';
import UserListControl from 'components/admin/user/list/';
import JobListControl from 'components/admin/job/list/';
import JobDetailControl from 'components/core/job/detail/';
import AppListControl from 'components/admin/app/list/';
import AppRepositoryControl from 'components/admin/app/repository/';
import SettingsGeneralControl from 'components/admin/settings/general/';
import SettingsServerControl from 'components/admin/settings/server/';
import SettingsMailControl from 'components/admin/settings/mail/';
import SettingsTemplatesControl from 'components/admin/settings/templates/';
import SettingsLogsControl from 'components/admin/settings/logs/';

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

  'pages/admin-home route': function(data) {
    this.activate({
      id: 'home',
      control: DashboardControl,
      options: {
        login: false
      }
    });
  },

  'pages/admin-jobs route': function(data) {
    this.activate({
      id: 'jobs',
      control: JobListControl,
      options: data
    });
  },

  'pages/admin-users route': function(data) {
    this.activate({
      id: 'users',
      control: UserListControl,
      options: data
    });
  },

  'pages/admin-apps route': function(data) {
    this.activate({
      id: 'apps',
      control: AppListControl,
      options: data
    });
  },

  'pages/admin-apps-repository route': function(data) {
    this.activate({
      id: 'apps',
      control: AppRepositoryControl,
      options: data
    });
  },

  'pages/admin-server route': function(data) {
    this.activate({
      id: 'apps',
      control: SettingsServerControl,
      options: data
    });
  },

  'pages/admin-settings-general route': function(data) {
    this.activate({
      id: 'apps',
      control: SettingsGeneralControl,
      options: data
    });
  },

  'pages/admin-settings-mail route': function(data) {
    this.activate({
      id: 'apps',
      control: SettingsMailControl,
      options: data
    });
  },

  'pages/admin-settings-templates route': function(data) {
    this.activate({
      id: 'apps',
      control: SettingsTemplatesControl,
      options: data
    });
  },

  'pages/admin-logs route': function(data) {
    this.activate({
      id: 'apps',
      control: SettingsLogsControl,
      options: data
    });
  },


  'jobs/:job route': function(data) {
    this.activate({
      id: 'jobs',
      control: JobDetailControl,
      options: {
        jobId: data.job,
        admin: true,
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
        admin: true,
        results: true
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
