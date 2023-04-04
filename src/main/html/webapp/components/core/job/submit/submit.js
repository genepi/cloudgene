import Control from 'can-control';
import domData from 'can-util/dom/data/data';
import $ from 'jquery';
import bootbox from 'bootbox';
import 'jquery-form';
import 'helpers/helpers';

import ErrorPage from 'helpers/error-page';
import Application from 'models/application';

import template from './submit.stache';
import templateUploadingDialog from './dialogs/uploading.stache';
import templateLabel from './controls/label.stache';
import templateSelect from './controls/select.stache';
import templateSelectBinded from './controls/select-binded.stache';
import templateRadio from './controls/radio.stache';
import templateCheckbox from './controls/checkbox.stache';
import templateFile from './controls/file.stache';
import templateFolder from './controls/folder.stache';
import templateFolderPattern from './controls/folder-pattern.stache';
import templateTermsCheckbox from './controls/terms-checkbox.stache';
import templateText from './controls/text.stache';
import templateTextarea from './controls/textarea.stache';


export default Control.extend({

  "init": function(element, options) {

    var that = this;

    Application.findOne({
      tool: options.app
    }, function(application) {
      that.application = application;
      $(element).hide();
      $(element).html(template({
        application: application,
        controls_label: templateLabel,
        controls_select: templateSelect,
        controls_radio: templateRadio,
        controls_text: templateText,
        controls_checkbox: templateCheckbox,
        controls_file: templateFile,
        controls_folder: templateFolder,
        controls_folder_pattern: templateFolderPattern,
        controls_terms_checkbox: templateTermsCheckbox,
        controls_textarea: templateTextarea,
        controls_select_binded: templateSelectBinded
      }));
      $(element).fadeIn();
      $("select").change();

    }, function(response) {
      new ErrorPage(element, response);
    });



  },

  '#parameters submit': function(form, event) {

    event.preventDefault();

    // check required parameters.
    if (form.checkValidity() === false) {
      form.classList.add('was-validated');
      return false;
    }

    //show upload dialog
    var uploadDialog = bootbox.dialog({
      message: templateUploadingDialog(),
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
          //do nothing.
        }
      }

      //submit form and upload files
      $(form).ajaxSubmit({
        dataType: 'json',

        headers: {
          "X-CSRF-Token": csrfToken
        },

        success: function(answer) {

          uploadDialog.modal('hide');

          if (answer.success) {

            window.location.href = '#!jobs/' + answer.id;

          } else {
            new ErrorPage("#content", {
              status: "",
              message: answer.message
            });

          }
        },

        error: function(response) {
          uploadDialog.modal('hide');
          new ErrorPage("#content", response);

        },

        //upade progress bar
        uploadProgress: function(event, position, total, percentComplete) {
          $("#waiting-progress").css("width", percentComplete + "%");
        }

      });

    });

    //show upload dialog. fires uploading files.
    uploadDialog.modal('show');

  },

  // custom file upload controls for single files

  '.select-control change': function(){
    this.application.updateBinding();
  },

  '#select-single-file-btn click': function(button) {
    // trigger click to open file dialog
    var fileUpload = $(button).closest('.col-sm-3').find(":file");
    fileUpload.trigger("click");
  },

  '.file-upload-field-single change': function(fileUpload) {
    var filenameControl = $(fileUpload).parent().find(".file-name-control");
    if (fileUpload.files.length > 0) {
      filenameControl.val(fileUpload.files[0].name);
    } else {
      filenameControl.val('');
    }
  },

  // custom file upload controls for multiple files

  '#select-files-btn click': function(button) {
    // trigger click to open file dialog
    var fileUpload = $(button).parent().find(":file");
    fileUpload.trigger("click");
  },

  '.file-upload-field-multiple change': function(fileUpload) {
    //update list of files
    var fileList = $(fileUpload).parent().find(".file-list");
    fileList.empty();
    for (var i = 0; i < fileUpload.files.length; i++) {
      fileList.append('<li><span class="fa-li"><i class="fas fa-file"></i></span>' + fileUpload.files[i].name + '</li>');
    }

    fileUpload.parent().find("#change-files");

    if (fileUpload.files.length > 0) {
      fileUpload.parent().find("#select-files").hide();
      fileUpload.parent().find("#change-files").show();
      fileUpload.parent().find("#remove-all-files").show();
    } else {
      fileUpload.parent().find("#select-files").show();
      fileUpload.parent().find("#change-files").hide();
      fileUpload.parent().find("#remove-all-files").hide();
    }
  },

  '#change-files-btn click': function(button) {
    // trigger click to open file dialog
    var fileUpload = $(button).parent().find(":file");
    fileUpload.trigger("click");
  },

  '#remove-all-files-btn click': function(button) {
    //clear hidden file upload field
    var fileUpload = $(button).parent().find(":file");
    fileUpload.val('');
    //clear list of files
    var fileList = $(button).parent().find(".file-list");
    fileList.empty();
    fileUpload.parent().find("#select-files").show();
    fileUpload.parent().find("#change-files").hide();
    fileUpload.parent().find("#remove-all-files").hide();
  }

});
