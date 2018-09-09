import can from 'can';

export default can.Model({
  findAll: 'GET /api/v2/admin/jobs',
  findOne: 'GET /api/v2/jobs/{id}/status',
  destroy: 'DELETE /api/v2/jobs/{id}'
}, {});
