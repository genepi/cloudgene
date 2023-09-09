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
import SettingsNextflowControl from 'components/admin/settings/nextflow/';
import SettingsServerControl from 'components/admin/settings/server/';
import SettingsMailControl from 'components/admin/settings/mail/';
import SettingsTemplatesControl from 'components/admin/settings/templates/';
import SettingsLogsControl from 'components/admin/settings/logs/';


$(document.links).filter(function () {
  return this.hostname != window.location.hostname;
}).attr('target', '_blank');

var routes = [{
  path: '',
  control: DashboardControl,
  options: {
    login: false
  },
  guard: adminGuard
}, {
  path: 'pages/admin-home',
  control: DashboardControl,
  options: {
    login: false
  },
  guard: adminGuard
}, {
  path: 'pages/jobs',
  control: JobListControl,
  guard: adminGuard
}, {
  path: 'pages/users',
  control: UserListControl,
  options: {
    page: 1
  },
  guard: adminGuard
}, {
  path: 'pages/users/pages/{page}',
  control: UserListControl,
  guard: adminGuard
}, {
  path: 'pages/users/search/{query}',
  control: UserListControl,
  guard: adminGuard
}, {
  path: 'pages/admin-apps',
  control: AppListControl,
  guard: adminGuard
}, {
  path: 'pages/admin-apps-repository',
  control: AppRepositoryControl,
  guard: adminGuard
}, {
  path: 'pages/admin-server',
  control: SettingsServerControl,
  guard: adminGuard
}, {
  path: 'pages/admin-settings-general',
  control: SettingsGeneralControl,
  guard: adminGuard
}, {
  path: 'pages/admin-settings-nextflow',
  control: SettingsNextflowControl,
  guard: adminGuard
}, {
  path: 'pages/admin-settings-mail',
  control: SettingsMailControl,
  guard: adminGuard
}, {
  path: 'pages/admin-settings-templates',
  control: SettingsTemplatesControl,
  guard: adminGuard
}, {
  path: 'pages/admin-logs',
  control: SettingsLogsControl,
  guard: adminGuard
}, {
  path: 'jobs/{job}',
  control: JobDetailControl,
  guard: adminGuard
}, {
  path: 'jobs/{job}/{tab}',
  control: JobDetailControl,
  guard: adminGuard
}];

function adminGuard(appState) {
  if (appState.attr('loggedIn')) {
    return appState.attr('user').attr('admin');
  } else {
    return false;
  }
}

$.ajaxPrefilter(function (options, orig, xhr) {
  if (!options.beforeSend) {
    options.beforeSend = function (xhr) {
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


Server.findOne({}, function (server) {

  new LayoutControl("#main", {
    appState: server
  });


  new RouterControl("#content", {
    routes: routes,
    appState: server,
    forbidden: {
      control: ErrorPage,
      options: {
        status: '403',
        responseText: 'Oops, you are not allowed to view this content.'
      }
    }
  });

});
