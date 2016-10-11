User = can.Model({
	findOne: 'GET /api/v2/users/{user}/profile',
  destroy: 'POST /api/v2/admin/users/delete',
  update: 'POST /api/v2/admin/users/changegroup',
  findAll: 'GET /api/v2/admin/users'
}, {});
