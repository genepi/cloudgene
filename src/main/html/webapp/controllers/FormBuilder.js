function buildForm(application, element, params, submitButtonText) {

	labelOpen = false;
	labelGroup = null;

	divGroup = $("<div></div>");
	divGroup.attr("class", "control-group");
	buildJobName(element);

	params.forEach(function(param, index) {

		param.id = "input-" + param.id;

		if (param.type == "group") {
			labelGroup = buildLabel2(element, param);
			labelOpen = true;
		} else {
			divGroup = $("<div></div>");
			divGroup.attr("class", "control-group");
			if (param.visible !== true) {
				divGroup.attr("style", "display:none");
			}

			if (param.type == "label") {
				divControls = $("<div></div>");
				divControls.html(param.description);
			} else if (param.type == "list" || param.type == "app_list") {
				buildLabel(divGroup, param);
				divControls = $("<div></div>");
				divControls.attr("class", "controls");
				buildList(divControls, param);
			} else if (param.type == "number") {
				buildLabel(divGroup, param);
				divControls = $("<div></div>");
				divControls.attr("class", "controls");
				buildNumber(divControls, param);
			} else if (param.type == "hdfs_file" || param.type == "local_file") {
				buildLabel(divGroup, param);
				divControls = $("<div></div>");
				divControls.attr("class", "controls");
				buildHdfsFile(divControls, param);
			} else if (param.type == "hdfs_folder" || param.type == "local_folder") {
				buildLabel(divGroup, param);
				divControls = $("<div></div>");
				divControls.attr("class", "controls");
				buildHdfsFolder(divControls, param);
			} else if (param.type == "checkbox") {
				divControls = $("<div></div>");
				divControls.attr("class", "controls");
				buildCheckbox(divControls, param);
				//buildLabel(divGroup, param);

			} else if (param.type == "agbcheckbox") {
				divControls = $("<div></div>");
				divControls.attr("class", "controls");
				buildAGBCheckbox(divControls, param);
			} else if (param.type == "number" || param.type == "text") {
				buildLabel(divGroup, param);
				divControls = $("<div></div>");
				divControls.attr("class", "controls");
				buildText(divControls, param);
			}

			if (param.type == "hdfs_folder" || param.type == "local_folder") {
				divControls.append('<span class="help-block">Multiple files can be selected by using the <span class="label">ctrl</span> / <span class="label">cmd</span> or <span class="label">shift</span> keys.</span>');
			} else {
				divControls.append('<span class="help-block"></span>');
			}

			divGroup.append(divControls);

			if (labelOpen) {

				labelGroup.append(divGroup);

			} else {

				$(element).append(divGroup);

			}

		}

	});

	// hidden field for application name
	inputField = $("<input></input>");
	inputField.attr("type", "hidden");
	inputField.attr("name", "tool");
	inputField.attr("value", application);

	$(element).append(inputField);


	// submit-button
	divGroup = $("<div></div>");
	divGroup.attr("class", "control-group");

	divControls = $("<div></div>");
	divControls.attr("class", "controls");


	buildSubmitButton(divControls, submitButtonText);

	divGroup.append(divControls);
	$(element).append(divGroup);
}

function buildLabel(element, param) {
 var caption = param.description;
	if (param.help){
		caption += '&nbsp;<a href="' + param.help + '" target="_blank"><i class="icon-question-sign"></i></a>';
	}
	var label = $('<label for="' + param.id + '" class="control-label">' + caption + '</label>');

	//label.text(param.description);
	//label.attr("for", param.id);
	//label.attr("class", "control-label");
	$(element).append(label);
}

function buildLabel2(element, param) {

	var accordion = $('<div></div>');
	accordion.attr('class', 'accordion');
	accordion.attr('id', 'accordion-' + param.id);

	var accordionGroup = $('<div></div>');
	accordionGroup.attr('class', 'accordion-group');
	accordion.append(accordionGroup);

	var accordionHeader = $('<div></div>');
	accordionHeader.attr('class', 'accordion-heading');
	accordionGroup.append(accordionHeader);

	var accordionLink = $('<a></a>');
	accordionLink.attr('class', 'accordion-toggle');
	accordionLink.attr('data-toggle', 'collapse');
	accordionLink.attr('data-parent', 'accordion-' + param.id);
	accordionLink.attr('href', '#my-accordion-' + param.id);

	accordionLink.text(param.description);
	accordionHeader.append(accordionLink);

	var accordionInner = $('<div></div>');
	accordionInner.attr('class', 'accordion-body collapse');
	accordionInner.attr('id', 'my-accordion-' + param.id);
	accordionGroup.append(accordionInner);

	$(element).append(accordion);

	return accordionInner;
}

function buildHdfsFile(element, param) {
	// var input = $("<input></input>");
	// input.attr("name",param.id);
	// input.attr("type","file");
	// $(element).append(input);

	temp = '<div class="fileupload fileupload-new" data-provides="fileupload">' + '<div class="input-append">' + '  <div class="uneditable-input">' + '		<i class="icon-file fileupload-exists"></i>' + '		<span class="fileupload-preview"></span>' + '	</div>' + '	<span class="btn btn-file">' + '	<span class="fileupload-new">Select file</span>' + '	<span class="fileupload-exists">Change</span>' + '	<input type="file" name ="' + param.id + '"' + (param.required ? 'data-required="true"' : '') + ' /></span>' + '	<a href="#" class="btn fileupload-exists" data-dismiss="fileupload">Remove</a>' + '  </div>' + '</div>';
	$(element).append(temp);

}

function buildHdfsFolder(element, param) {

	//dummy: upload -> value: upload, sftp: value: link;user;password
	var inputParameter = $("<input></input>");
	inputParameter.attr("name", param.id);
	inputParameter.attr("type", "hidden");
	inputParameter.attr("value", "upload");
	inputParameter.attr("data-required", "true");
	$(element).append(inputParameter);

	//input source selection
	var select = $("<select></select>");
	select.attr("name", param.id + "-source");

	var option = $("<option></option>");
	option.attr("value", "upload");
	option.text("File Upload");
	option.attr("selected", "selected");
	select.append(option);

	option = $("<option></option>");
	option.attr("value", "http");
	option.text("URLs (HTTP) ");
	select.append(option);

	option = $("<option></option>");
	option.attr("value", "sftp");
	option.text("Secure File Transfer Protocol (SFTP)");
	select.append(option);

	option = $("<option></option>");
	option.attr("value", "ftp");
	option.text("File Transfer Protocol (FTP) ");
	//select.append(option);

	$(element).append(select);

	//file upload div
	temp = '<div id="' + param.id + '-upload-div">' + '<div class="fileupload fileupload-new" data-provides="fileupload" id="' + param.id + '-upload-widget">' + '  <ul class="my-fileupload-preview uneditable-input" style="height: 150px; width: 600px;"></ul>' + '  <div id="' + param.id + '-upload-2">' + '    <span class="btn btn-file"><span class="fileupload-new">Select Files</span><span class="fileupload-exists">Change</span>' + '	<input type="file" id ="' + param.id + '-upload" name ="' + param.id + '-upload" ' +
		(param.required ? 'data-required="true"' : '') +
		' multiple="" /></span>' + '    <a href="#" class="btn fileupload-exists" data-dismiss="fileupload">Remove All</a>' + '  </div>' + '</div>' + '  </div>';

	$(element).append(temp);

	//extern source div

	var div = $("<div></div>");
	div.attr("id", param.id + "-extern");
	div.attr("style", "display:none;");

	var filelist = $("<ul></ul>");
	filelist.attr("class", "my-fileupload-preview uneditable-input");
	filelist.attr("id", param.id + "-extern-filelist");
	filelist.attr("style", "height: 150px; width: 600px;");
	div.append(filelist);

	div.append("<br>");

	var addFiles = $('<span></span>');
	addFiles.attr("class", "btn");
	addFiles.text("Add Files");
	addFiles.click(function() {

		$("#" + param.id + "-extern-dialog").modal('show');

	});
	div.append(addFiles);
	div.append("<br>");

	var divDialog = $("<div></div>");
	divDialog.attr("id", param.id + "-extern-dialog");
	divDialog.attr("class", "modal hide");

	var divDialog2 = $("<div></div>");
	divDialog2.attr("class", "modal-dialog");
	divDialog.append(divDialog2);

	var divDialogContent = $("<div></div>");
	divDialogContent.attr("class", "modal-content");
	divDialog2.append(divDialogContent);

	var divDialogHeader = $("<div></div>");
	divDialogHeader.attr("class", "modal-header");
	divDialogHeader.append('<h4 class="modal-title" id="protocol-title">Secure File Transfer Protocol (SFTP)</h4>');
	divDialogContent.append(divDialogHeader);

	var divDialogBody = $("<div></div>");
	divDialogBody.attr("class", "modal-body");
	divDialogContent.append(divDialogBody);

	var helpText = $("<p id=\"help-text\"></p>");
	helpText.attr("class", "text");
	helpText.text("Please enter your SFTP connection informations.");
	divDialogBody.append(helpText);

	var divLogin = $("<div></div>");
	divLogin.attr("id", param.id + "-extern-login");
	divLogin.attr("style", "display:none;");
	divDialogBody.append(divLogin);

	var usernameLabel = $("<label></label>");
	usernameLabel.text("Username");
	divLogin.append(usernameLabel);

	var username = $("<input></input>");
	username.attr("type", "text");
	username.attr("name", param.id + "-lf-user");
	divLogin.append(username);

	var passwordLabel = $("<label></label>");
	passwordLabel.text("Password");
	divLogin.append(passwordLabel);

	var password = $("<input></input>");
	password.attr("type", "password");
	password.attr("name", param.id + "-lf-secret");
	divLogin.append(password);

	var passwordLocation = $("<label></label>");
	passwordLocation.text("URLs:");
	divLogin.append(passwordLocation);

	var textarea = $("<textarea></textarea>");
	textarea.attr("name", param.id + "-lf");
	textarea.attr("id", param.id + "-lf");
	textarea.attr("style", "height: 120px; width: 500px;");
	divDialogBody.append(textarea);

	divDialogBody.append('<p class="muted" id="example-text"><i>e.g. sftp://192.168.71.201/home/user/data/</i>');

	var errorText = $("<div></div>");
	errorText.attr("class", "alert alert-error");
	errorText.hide();
	divDialogBody.append(errorText);

	var divDialogFooter = $("<div></div>");
	divDialogFooter.attr("class", "modal-footer");
	divDialogContent.append(divDialogFooter);

	var buttonClose = $("<button></button>");
	buttonClose.attr("type", "button");
	buttonClose.attr("class", "btn btn-default");
	buttonClose.attr("data-dismiss", "modal");
	buttonClose.text("Close");
	divDialogFooter.append(buttonClose);

	var buttonAddFiles = $("<button></button>");
	buttonAddFiles.attr("type", "button");
	buttonAddFiles.attr("class", "btn btn-primary");
	buttonAddFiles.text("Add Files");
	divDialogFooter.append(buttonAddFiles);

	buttonAddFiles.click(function() {

		errorText.hide();

		if (!username.attr("display") == 'none') {

			if (textarea.attr("value").trim() == '' || username.attr("value").trim() == '' || password.attr("value").trim() == '') {
				errorText.show();
				errorText.empty();
				errorText.append("<b>Error: </b> Please enter username, password and the URLs auf your datasets.");
				return;
			}

		}

		if (!textarea.attr("value").trim().toString().startsWith("sftp://") && !textarea.attr("value").trim().toString().startsWith("http://") && !textarea.attr("value").trim().toString().startsWith("https://") && !textarea.attr("value").trim().toString().startsWith("ftp://")) {
			errorText.show();
			errorText.empty();
			errorText.append("<b>Error: </b> The URLs of your datasets is not valid and cannot be loaded.");
			return;
		}

		buttonAddFiles.button('loading');

		$.ajax({
			url: "api/v2/importer/files",
			type: "POST",
			data: {
				input: textarea.attr("value") + ";" + username.attr("value") + ";" + password.attr("value")
			},
			success: function(data) {

				errorText.hide();
				$("#" + param.id + "-extern-filelist").empty();

				var arr = $.parseJSON(data);
				$.each(arr, function(index, value) {

					var fileItem = $("<li></li>");
					fileItem.append('<i class="icon-file"></i> ' + value["text"].toString());
					$("#" + param.id + "-extern-filelist").append(fileItem);

				});

				$("#" + param.id + "-extern-dialog").modal('hide');
				buttonAddFiles.button('reset');

				//update value
				inputParameter.attr("value", textarea.attr("value") + ";" + username.attr("value") + ";" + password.attr("value"));

			},
			error: function(message) {

				errorText.show();
				errorText.empty();
				errorText.append("<b>Error: </b>" + message.responseText);
				buttonAddFiles.button('reset');

			}
		});

	});

	select.change(function() {

		//clear file upload list
		$("#" + param.id + "-upload-widget").find('[data-dismiss="fileupload"]').click();

		//clear sftp file list
		$("#" + param.id + "-extern-filelist").empty();
		textarea.attr("value", "");
		username.attr("value", "");
		password.attr("value", "");

		if ($(this).val() == 'upload') {
			$('#' + param.id + '-upload-div').show();
			$('#' + param.id + '-upload').attr('disabled', false);
			$('#' + param.id + '-extern').hide();
			inputParameter.attr("value", "upload");
		} else {
			$('#' + param.id + '-extern').show();
			$('#' + param.id + '-upload-div').hide();
			$('#' + param.id + '-upload').attr('disabled', true);
			inputParameter.attr("value", "");
		}

		if ($(this).val() == 'sftp') {
			$('#' + param.id + '-extern-login').show();
			$('#' + param.id + '-lf').text("sftp://");
			$('#protocol-title').text("Secure File Transfer Protocol (SFTP)");
			$('#help-text').text("Please enter your SFTP connection informations.");
			$('#example-text').text('e.g. sftp://192.168.71.201/home/user/data/');
		}

		if ($(this).val() == 'http') {
			$('#' + param.id + '-extern-login').hide();
			$('#' + param.id + '-lf').text("http://");
			$('#protocol-title').text("Download data from web");
			$('#help-text').text("Please enter your URLs.");
			$('#example-text').text('e.g. http://www.example.com/test-data.txt');
		}
		if ($(this).val() == 'ftp') {
			$('#' + param.id + '-extern-login').show();
			$('#' + param.id + '-lf').text("ftp://");
		}

	});

	div.append(divDialog);

	$(element).append(div);
}

function buildList(element, param) {
	var defaultSelection = false;
	var select = $("<select></select>");
	select.attr("name", param.id);
	if (param.readOnly) {
		select.attr("disabled", "true");
	}

	if (param.required) {
		select.attr("data-required", "true");
	}

	var emptyOption = $("<option></option>");
	emptyOption.attr("value", "---empty---");
	emptyOption.text("-- select an option --");
	select.append(emptyOption);


	if (param.attr('values')){
		for (var key in param.attr('values').attr()) {
			var option = $("<option></option>");
			option.attr("value", key);
			option.text(param.values[key]);
			if (param.value == key) {
				option.attr("selected", "");
				defaultSelection = true;
			}
			select.append(option);
		}
	}
	if (!defaultSelection) {
		emptyOption.attr("selected", "");
	}

	$(element).append(select);

}


function buildText(element, param) {
	var input = $("<input></input>");
	input.attr("name", param.id);
	input.attr("type", "text");
	input.attr("value", param.value);
	if (param.readOnly) {
		input.attr("disabled", "true");
	}
	if (param.required) {
		input.attr("data-required", "true");
	}
	$(element).append(input);
}

function buildJobName(element) {

	divGroup = $("<div></div>");
	divGroup.attr("class", "control-group");

	var label = $('<label for="job-name" class="control-label">Name</label>');
	$(divGroup).append(label);

	divControls = $("<div></div>");
	divControls.attr("class", "controls");

	var input = $("<input></input>");
	input.attr("name", "job-name");
	input.attr("type", "text");
	input.attr("value", "");
  input.attr("placeholder","optional job name");
	$(divControls).append(input);

	divGroup.append(divControls);
	$(element).append(divGroup);

  $(element).append("<hr/>");
}


function buildNumber(element, param) {
	buildText(element, param);
}

function buildCheckbox(element, param) {

	var label = $("<label></label>");
	label.attr('class', "checkbox");

	var input = $("<input></input>");
	input.attr("id", param.id);
	input.attr("name", param.id);
	input.attr("type", "checkbox");
	input.attr("value", param.value);
	if (param.readOnly) {
		input.attr("disabled", "true");
	}

	if (param.values["true"] == param.value) {
		input.attr("checked", "true");
	}

	$(label).append(input);
	$(label).append(param.description);

	$(element).append(label);

	//input.bootstrapSwitch();
}

function buildAGBCheckbox(element, param) {

	var label = $("<label></label>");
	label.attr('class', "checkbox");

	var input = $("<input></input>");
	input.attr("id", param.id);
	input.attr("name", param.id);
	input.attr("type", "checkbox");
	input.attr("value", param.value);
	input.attr("class", "agb");
	$(label).append(input);
	$(label).append(param.description);

	$(element).append(label);
}

function buildSubmitButton(element, submitButtonText) {
	var input = $("<button></button>");
	input.attr("id", "save");
	input.attr("class", "btn btn-primary");
	input.attr("data-loading-text", "Uploading...");
	input.text(submitButtonText);
	$(element).append(input);
}

if (typeof String.prototype.startsWith != 'function') {
	String.prototype.startsWith = function(str) {
		return this.slice(0, str.length) == str;
	};
}
