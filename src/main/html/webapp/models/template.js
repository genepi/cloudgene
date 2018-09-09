import can from 'can';

export default can.Model({
  findAll: 'GET /api/v2/admin/server/templates',
  findOne: 'GET /api/v2/admin/server/templates/{key}',
  destroy: 'POST /api/v2/admin/server/templates/delete',
  create: 'POST /api/v2/admin/server/templates/{key}',
  update: 'POST /api/v2/admin/server/templates/{key}'
}, {});
