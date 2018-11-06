import 'can-map-define';
import Model from 'can-connect/can/model/model';



export default Model.extend({
  findAll: 'GET /api/v2/jobs',
  findOne: 'GET /api/v2/jobs/{id}/status',
  destroy: 'DELETE /api/v2/jobs/{id}',
}, {
  define: {

    'longName': {
      get: function() {
        return this.attr('id') != this.attr('name') ? this.attr('name') : this.attr('id');
      }
    },

    'executionTime': {
      get: function() {
        var start = this.attr('setupStartTime') + this.attr('startTime');
        var end = this.attr('setupEndTime') + this.attr('endTime');
        var current = this.attr('currentTime');

        if (start === 0 && end === 0) {
          return undefined;
        }

        var executionTime = 0;
        if (start > 0 && end === 0) {
          executionTime = current - start;
        } else {
          executionTime = end - start;
        }

        if (executionTime <= 0) {

          return undefined;

        } else {
          return executionTime;
        }

      }
    },

    'stateAsText': {
      get: function() {
        if (this.attr('state') == 1) {
          return 'Waiting';
        } else if (this.attr('state') == 2) {
          return 'Running';
        } else if (this.attr('state') == 3) {
          return 'Exporting Data';
        } else if (this.attr('state') == 4) {
          return 'Complete';
        } else if (this.attr('state') == 5) {
          return 'Error';
        } else if (this.attr('state') == 6) {
          return 'Canceled';
        } else {
          return 'Error';
        }
      }
    },

    'stateAsClass': {
      get: function() {

        if (this.attr('state') == '-1') {
          return 'dark';
        }
        if (this.attr('state') == '1') {
          if (this.attr('setupRunning')) {
            return 'secondary';
          } else {
            return 'secondary';
          }
        }
        if (this.attr('state') == '2') {
          return 'primary';
        }
        if (this.attr('state') == '3') {
          return 'primary';
        }
        if (this.attr('state') == '4' || this.attr('state') == '8') {
          return 'success';
        }
        if (this.attr('state') == '5') {
          return 'danger';
        }
        if (this.attr('state') == '6') {
          return 'danger';
        }
        if (this.attr('state') == '7') {
          return 'dark';
        }
      }
    },

    'isInQueue': {
      get: function() {
        return this.attr('state') == 1 && this.attr('positionInQueue') != -1;
      }
    },

    'isPending': {
      get: function() {
        return this.attr('state') == '-1';
      }
    },

    'isRetired': {
      get: function() {
        return this.attr('state') == '7';
      }
    },

    'willBeRetired': {
      get: function() {
        return this.attr('state') == 8 || this.attr('state') == 9;
      }
    },

    'canResetCounters': {
      get: function() {
        return this.attr('state') > 3 || this.attr('state') == -1;
      }
    },

    'canSendRetireNotification': {
      get: function() {
        return this.attr('state') != '8' && this.attr('state') != '9';
      }
    },

    'canIncreaseRetireDate': {
      get: function() {
        return this.attr('state') == '8' || this.attr('state') == '9';
      }
    },

    'canShowLog': {
      get: function() {
        return this.attr('logs') != undefined &&  this.attr('logs') != '';
      }
    },

    'canCancel': {
      get: function() {
        return this.attr('state') <= '3' && this.attr('state') != '-1';
      }
    },

    'canRetireJob': {
      get: function() {
        return this.attr('state') == '4' || this.attr('state') != '5' || this.attr('state') != '6';
      }
    },

    'canDelete': {
      get: function() {
        return this.attr('state') > '3' || this.attr('state') == '-1';
      }
    }
  }
});
