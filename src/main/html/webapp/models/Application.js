Application = can.Model({
    findAll: 'GET /api/v2/server/apps',
    findOne: 'GET /api/v2/server/apps/{tool}',
    create: 'POST /api/v2/server/apps',
    destroy: 'DELETE /api/v2/server/apps/{id}'
}, {

});
