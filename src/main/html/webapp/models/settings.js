import can from 'can/legacy';

export default can.Model({
  findOne: 'GET /api/v2/admin/server/settings',
  create: 'POST /api/v2/admin/server/settings/update',
  update: 'POST /api/v2/admin/server/settings/update'
}, {});
