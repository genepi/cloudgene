//model
Template = can.Model({
	findAll: 'GET /api/v2/admin/server/templates',
	findOne: 'POST /api/v2/admin/server/templates',
	destroy: 'POST /api/v2/admin/server/templates/delete',
	create: 'POST /api/v2/admin/server/templates/update',
	update: 'POST /api/v2/admin/server/templates/update'
}, {});
