import Model from 'can-connect/can/model/model';

export default Model.extend({
  destroy: 'DELETE /api/v2/users/{user}/profile'
}, {

});
