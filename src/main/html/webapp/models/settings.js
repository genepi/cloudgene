import Model from 'can-connect/can/model/model';

export default Model.extend({
  findOne: 'GET /api/v2/admin/server/settings',
  create: 'POST /api/v2/admin/server/settings/update',
  update: 'POST /api/v2/admin/server/settings/update'
}, {});
