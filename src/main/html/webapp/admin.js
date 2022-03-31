import $ from 'jquery';
import 'popper.js';
import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.css';
import 'components/core/layout/layout.css';
import '@fortawesome/fontawesome-free/css/all.css';
import 'can-map-define';

import Server from 'models/server';
import ErrorPage from 'helpers/error-page';
import LayoutControl from 'components/admin/layout/';
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
  path: 'pages/jobs',
  control: JobListControl
}, {
  path: 'pages/users',
  control: UserListControl,
  options: {
    page: 1
  }
}, {
  path: 'pages/users/pages/{page}',
  control: UserListControl,
},{
  path: 'pages/users/search/{query}',
  control: UserListControl,
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
  path: 'jobs/{job}',
  control: JobDetailControl,
}, {
  path: 'jobs/{job}/{tab}',
  control: JobDetailControl,
}];

$.ajaxPrefilter(function(options, orig, xhr) {
  if (!options.beforeSend) {
    options.beforeSend = function(xhr) {
      if (localStorage.getItem("cloudgene")) {
        try {
          // get data
          var data = JSON.parse(localStorage.getItem("cloudgene"));
          xhr.setRequestHeader("X-CSRF-Token", data.csrf);
          xhr.setRequestHeader("X-Auth-Token", data.token);
        } catch (e) {
          // do nothing
        }
      }
    }
  }
  //canjs has an bug while sending data in json format: data is not in json format, so we need to fix it convert it manually to JSON
  if (options.processData &&
    /^application\/json((\+|;).+)?$/i.test(options.contentType) &&
    /^(post|put|delete)$/i.test(options.type)
  ) {
    options.data = JSON.stringify(orig.data);
    options.processData = false;
  }

});


Server.findOne({}, function(server) {

  new LayoutControl("#main", {});


  new RouterControl("#content", {
    routes: routes,
    appState: server,
    forbidden: {
      control: ErrorPage,
      options: {
        status: '401',
        message: 'Oops, you need to <a href="#!pages/login">login</a> to view this content.'
      }
    }
  });

});
