import can from 'can/legacy';

export default can.Model({
  findAll: 'GET /api/v2/jobs',
  findOne: 'GET /api/v2/jobs/{id}/status',
  destroy: 'DELETE /api/v2/jobs/{id}'
}, {});
