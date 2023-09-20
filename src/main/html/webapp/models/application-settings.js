import Model from 'can-connect/can/model/model';

export default Model.extend({
  findOne: 'GET /api/v2/server/apps/{id}/settings',
  update: 'PUT /api/v2/server/apps/{id}/settings'
}, {

});
