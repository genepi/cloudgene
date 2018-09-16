import Model from 'can-connect/can/model/model';

export default Model.extend({
  findOne: 'GET /api/v2/users/{user}/api-token',
  create: 'POST /api/v2/users/{user}/api-token',
  destroy: 'DELETE /api/v2/users/{user}/api-token',
}, {});
