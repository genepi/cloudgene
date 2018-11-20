import Model from 'can-connect/can/model/model';

export default Model.extend({
  update: 'GET /api/v2/jobs/{id}/{action}'
}, {});
