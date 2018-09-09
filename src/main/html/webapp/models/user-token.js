import can from 'can';

export default can.Model({
  findOne: 'GET /api/v2/users/{user}/api-token',
  create: 'POST /api/v2/users/{user}/api-token',
  destroy: 'DELETE /api/v2/users/{user}/api-token',
}, {});
