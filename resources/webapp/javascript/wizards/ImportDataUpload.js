/*******************************************************************************
 * Cloudgene: A graphical MapReduce interface for cloud computing
 * 
 * Copyright (C) 2010, 2011 Sebastian Schoenherr, Lukas Forer
 *  
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

Ext.ns('MapRed.wizards');

MapRed.wizards.ImportDataUpload = Ext.extend(Ext.ux.Wiz, {

	folder: "",

	initComponent : function() {
		Ext.apply(this, {

			id : 'wizard',
			previousButtonText : '&lt; Back',
			title : 'Import Data',
			headerConfig : {
				title : 'Import Data',
				image : '../images/import-data.png',
				stepText : '{2}'
			},
			loadMaskConfig : {
				'default' : 'Please wait, validating input...'
			},
			cardPanelConfig : {
				defaults : {

					bodyStyle : 'padding:13px;background-color:#ffffff;',
					border : false

				}
			},
			width : 480,
			height : 500,
			cards : [ new MapRed.wizards.ImportDataUploadCard({folder: this.folder}) ],
			listeners : {
				finish : this.onFinish
			}

		});

		MapRed.wizards.ImportDataUpload.superclass.initComponent.apply(this,
				arguments);

	},

	onFinish : function() {

		var values = {};
		var formValues = {};
		var eins = this;
		mycards = this.getWizardData();
		this.showLoadMask(true, 'validating');
		this.switchDialogState(false);

		formValues = this.cards[0].form.getValues(false);
		for ( var a in formValues) {
			values[a] = formValues[a];
		}
		var form = this.cards[0].getForm();
		if (form.isValid()) {
			form.submit({
				url : '../hdfs/upload',
				waitTitle : 'File Upload',
				fileUpload : true,
				disableCaching : true,

				params : {
					path : values.path
				},
				waitMsg : 'Uploading file...',
				failure : function(form, v) {
					var serverResponse = JSON.parse(v.response.responseText);
					eins.switchDialogState(true);
					if (serverResponse.type == "exception") {

						Ext.Msg.show({
							title : 'Upload error',
							msg : exceptionsMsgTemplate.apply({
								message : serverResponse.exception.message
							}),
							modal : false,
							icon : Ext.Msg.ERROR,
							buttons : Ext.Msg.OK
						});
					} else {
						Ext.Msg.show({
							title : 'Error occurred',
							msg : serverResponse.message,
							modal : false,
							icon : Ext.Msg.INFO,
							buttons : Ext.Msg.OK
						});
					}
				},

				success : function(response, request) {
					Ext.Msg.show({
						title : 'Success',
						msg : 'File sucessfully uploaded',
						buttons : Ext.Msg.OK
					});
					eins.switchDialogState(true);
					eins.close();

					// refresh tables
					var jobTable = Ext.getCmp('jobtable');
					var storeJobs = jobTable.getStore();
					storeJobs.reload();
				}
			});
		} else {
			alert("File upload form invalid");
		}

	}

});

MapRed.wizards.ImportDataUploadCard = Ext
		.extend(
				Ext.ux.Wiz.Card,
				{

					folder: "",

					initComponent : function() {

						Ext
								.apply(
										this,
										{
											id : 'card2',
											wizRef : this,
											title : 'File Upload',
											monitorValid : true,
											frame : false,
											fileUpload : true,
											border : false,
											height : '100%',
											defaults : {
												labelStyle : 'font-size:11px'
											},
											folder: this.folder,
											items : [
													{
														border : false,
														bodyStyle : 'background:none;padding-bottom:30px;',
														html : 'Enter a folder name and upload a file.'
													},
													{
														title : '',
														id : 'fieldset-target',
														xtype : 'fieldset',
														autoHeight : true,
														defaults : {
															width : 210,
															labelStyle : 'font-size:11px'
														},
														defaultType : 'textfield',
														items : [ new Ext.form.TextField(
																{
																	id : 'path',
																	fieldLabel : 'Folder Name',
																	allowBlank : false,
																	value: this.folder
																}) ]
													},
													{
														title : 'File Upload',
														id : 'fieldset-upload',
														xtype : 'fieldset',
														autoHeight : true,
														defaults : {
															width : 210,
															labelStyle : 'font-size:11px',
														},
														defaultType : 'textfield',
														items : [
																new Ext.form.FileUploadField(
																		{
																			id : 'form-file',
																			emptyText : 'Upload a file.',
																			autoWidth : false,
																			fieldLabel : 'Filename',
																			anchor : '100%',
																			width : '150px',
																			allowBlank : false,
																			name : 'sampleFile'
																		}),
																{
																	xtype : 'panel',
																	border : false,
																	anchor : '100%',
																	html : '<p style="text-align:right;color:#888;">Uploaded zip files are extracted automatically.<p>'
																} ]
													} ]
										});

						// call parent
						MapRed.wizards.ImportDataUploadCard.superclass.initComponent
								.apply(this, arguments);

					}

				});
