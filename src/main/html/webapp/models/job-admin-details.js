import Model from 'can-connect/can/model/model';

export default Model.extend({
  findAll: 'GET /api/v2/admin/jobs',
  findOne: 'GET /api/v2/jobs/{id}/status',
  destroy: 'DELETE /api/v2/jobs/{id}'
}, {});
