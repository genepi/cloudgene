import $ from 'jquery';
import 'popper.js';
import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
import 'assets/css/cloudgene.css';
import '@fortawesome/fontawesome-free/css/all.css';

import RouterControl from 'helpers/router';
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


$(document.links).filter(function() {
  return this.hostname != window.location.hostname;
}).attr('target', '_blank');

var routes = [{
  path: '',
  control: DashboardControl,
  options: {
    login: false
  }
}, {
  path: 'pages/admin-home',
  control: DashboardControl,
  options: {
    login: false
  }
}, {
  path: 'pages/admin-jobs',
  control: JobListControl
}, {
  path: 'pages/admin-users',
  control: UserListControl
}, {
  path: 'pages/admin-apps',
  control: AppListControl
}, {
  path: 'pages/admin-apps-repository',
  control: AppRepositoryControl
}, {
  path: 'pages/admin-server',
  control: SettingsServerControl
}, {
  path: 'pages/admin-settings-general',
  control: SettingsGeneralControl
}, {
  path: 'pages/admin-settings-mail',
  control: SettingsMailControl
}, {
  path: 'pages/admin-settings-templates',
  control: SettingsTemplatesControl
}, {
  path: 'pages/admin-logs',
  control: SettingsLogsControl
}, {
  path: 'jobs/:job ',
  control: JobDetailControl,
  options: {
    admin: true,
    results: false
  }
}, {
  path: 'jobs/:job/results',
  control: JobDetailControl,
  options: {
    admin: true,
    results: true
  }
}];

new RouterControl("#content", {
  routes: routes
});
