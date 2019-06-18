import Model from 'can-connect/can/model/model';

export default Model.extend({
  findAll: 'GET /api/v2/server/apps',
  findOne: 'GET /api/v2/server/apps/{tool}',
  create: 'POST /api/v2/server/apps',
  update: 'PUT /api/v2/server/apps/{id}',
  destroy: 'DELETE /api/v2/server/apps/{id}'
}, {

  'updateBinding': function() {

    var that = this;

    this.params.each(function(param) {
      if (param.attr('type') === 'binded_list') {
        var bindInput = param.attr('bind');
        var bindParam = that.getParamById(bindInput);
        param.attr('values').each(function(value) {
          var enabled = (value.attr('key') == bindParam.attr('value'));
          value.attr('enabled', enabled);
        });
      }
    });
  },

  'getParamById': function(id) {
    var paramFound = undefined;
    this.params.each(function(param) {
      if (param.attr('id') === id) {
        paramFound = param;
      }
    });
    return paramFound;
  }



});
