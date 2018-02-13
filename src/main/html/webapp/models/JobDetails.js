// Job Details Model
JobDetails = can.Model.extend({
	findOne: 'GET /api/v2/jobs/{id}',
	destroy: 'DELETE /api/v2/jobs/{id}'
}, {});
