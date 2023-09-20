import Model from 'can-connect/can/model/model';

export default Model.extend({
  findOne: 'GET /api/v2/admin/server/nextflow/config',
  create: 'POST /api/v2/admin/server/nextflow/config/update',
  update: 'POST /api/v2/admin/server/nextflow/config/update'
}, {});
