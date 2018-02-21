// controller
SubmitJobPage = can.Control({

  "init": function(element, options) {
    that = this;
    Application.findOne({
      tool: options.tool
    }, function(application) {
      element.hide();
      element.html(can.view('views/run.ejs', {
        application: application
      }));
      element.fadeIn();

    }, function(message) {
      new ErrorPage(that.element, {
        status: message.statusText,
        message: message.responseText
      });
    });

  },

  '#parameters submit': function(form) {

    // check required parameters.
    if (form[0].checkValidity() === false) {
      form[0].classList.add('was-validated');
      return false;
    }

    //show upload dialog
    var uploadDialog = bootbox.dialog({
      message: can.view('views/run.uploading.ejs'),
      closeButton: false,
      className: 'upload-dialog',
      shown: false
    });

    //start uploading when dialog is shown
    uploadDialog.on('shown.bs.modal', function() {

      var csrfToken;
      if (localStorage.getItem("cloudgene")) {
        try {

          // get data
          var data = JSON.parse(localStorage.getItem("cloudgene"));
          csrfToken = data.csrf;

        } catch (e) {

        }
      }

      //submit form and upload files
      form.ajaxSubmit({
        dataType: 'json',

        headers: {
          "X-CSRF-Token": csrfToken
        },

        success: function(answer) {

          uploadDialog.modal('hide');

          if (answer.success) {
            can.route('jobs/:job');
            can.route.attr({
              route: 'jobs/:job',
              job: answer.id,
              page: 'jobs'
            });

          } else {
            new ErrorPage("#content", {
              status: "",
              message: answer.message
            });

          }
        },

        error: function(message) {
          uploadDialog.modal('hide');
          new ErrorPage("#content", {
            status: message.statusText,
            message: message.responseText
          });

        },

        //upade progress bar
        uploadProgress: function(event, position, total, percentComplete) {
          $("#waiting-progress").css("width", percentComplete + "%");
        }

      });

    });

    //show upload dialog. fires uploading files.
    uploadDialog.modal('show');
    return false;
  }

});
