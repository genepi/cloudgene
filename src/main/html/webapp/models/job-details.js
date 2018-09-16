import Model from 'can-connect/can/model/model';

export default Model.extend({
  findOne: 'GET /api/v2/jobs/{id}',
  destroy: 'DELETE /api/v2/jobs/{id}'
}, {});
