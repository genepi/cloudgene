import can from 'can';

export default can.Model({
  findAll: 'GET /api/v2/server/apps',
  findOne: 'GET /api/v2/server/apps/{tool}',
  create: 'POST /api/v2/server/apps',
  update: 'PUT /api/v2/server/apps/{id}',
  destroy: 'DELETE /api/v2/server/apps/{id}'
}, {

});
