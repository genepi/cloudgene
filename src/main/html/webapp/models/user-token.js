import Model from 'can-connect/can/model/model';

export default Model.extend({
  create: 'POST /api/v2/users/{user}/api-token?expiration={expiration}',
  destroy: 'DELETE /api/v2/users/{user}/api-token',
}, {});
