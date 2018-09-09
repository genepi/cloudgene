import can from 'can';

export default can.Model.extend({
  update: 'GET /api/v2/jobs/{id}/{action}'
}, {});
