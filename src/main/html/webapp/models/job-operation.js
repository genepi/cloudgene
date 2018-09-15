import can from 'can/legacy';

export default can.Model.extend({
  update: 'GET /api/v2/jobs/{id}/{action}'
}, {});
