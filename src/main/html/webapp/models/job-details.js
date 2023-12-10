import Job from 'models/job';

export default Job.extend({
  findOne: 'GET api/v2/jobs/{id}',
  destroy: 'DELETE api/v2/jobs/{id}'
}, {});
