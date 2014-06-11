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

MapRed.wizards.ImportDataFtp = Ext.extend(Ext.ux.Wiz, {

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
			cards : [ new MapRed.wizards.ImportDataFtpCard({folder: this.folder}) ],
			listeners : {
				// defined in addSessionHandler
				finish : this.onFinish
			}

		});

		MapRed.wizards.ImportDataFtp.superclass.initComponent.apply(this,
				arguments);

	},

	onFinish : function() {

		mycards = this.getWizardData();
		this.showLoadMask(true, 'validating');
		this.switchDialogState(false);
		var values = {};
		var formValues = {};
		for ( var i = 0, len = this.cards.length; i < len; i++) {
			formValues = this.cards[i].form.getValues(false);
			for ( var a in formValues) {
				values[a] = formValues[a];
			}
		}

		Ext.Ajax.request({
			url : '../hdfs/import',

			method : 'POST',
			scope : this,
			jsonData : values,

			failure : function(response, request) {
				this.switchDialogState(true);
				Ext.Msg.show({
					title : 'Exception',
					msg : response.responseText,
					buttons : Ext.Msg.OK
				});
			},
			success : function(response, request) {
				Ext.Msg.show({
					title : 'Successfully added!',
					msg : response.responseText,
					buttons : Ext.Msg.OK
				});
				this.switchDialogState(true);
				this.close();

				// refresh tables
				var jobTable = Ext.getCmp('jobtable');
				var storeJobs = jobTable.getStore();
				storeJobs.reload();
			}
		});

	}
});

Ext.ns('MapRed.wizards');

MapRed.wizards.ImportDataFtpCard = Ext.extend(Ext.ux.Wiz.Card, {

	serverField : null,

	userField : null,

	passwordField : null,

	ftpModeBox : null,

    folder: "",

	initComponent : function() {

		this.serverField = new Ext.form.TextField({
			id : "server",
			name : 'server',
			value : 'ftp://',
			fieldLabel : 'FTP-Server',
			allowBlank : false
		});

		this.userField = new Ext.form.TextField({
			id : "username",
			name : 'username',
			fieldLabel : 'Username',
			value : '',
			allowBlank : true,
			hidden : true
		});

		this.passwordField = new Ext.form.TextField({
			id : "password",
			name : 'password',
			fieldLabel : 'Password',
			allowBlank : true,
			inputType : 'password',
			value : '',
			hidden : true
		});

		this.ftpModeBox = new Ext.form.RadioGroup({
			fieldLabel : 'Mode',
			vertical : false,
			id : "group1",
			items : [ {
				boxLabel : 'Anonymous',
				name : 'buttonMode',
				inputValue : '2',
				checked : true,
				userField : this.userField,
				passwordField : this.passwordField,
				listeners : {
					'check' : function(checkbox, checked) {
						if (checked) {
							this.userField.setVisible(false);
							this.passwordField.setVisible(false);
						}
					}
				}
			}, {
				boxLabel : 'Standard Login',
				name : 'buttonMode',
				inputValue : '1',
				userField : this.userField,
				passwordField : this.passwordField,
				listeners : {
					'check' : function(checkbox, checked) {
						if (checked) {
							this.userField.setVisible(true);
							this.passwordField.setVisible(true);
						}
					}
				}
			} ]

		});

		Ext.apply(this, {
			id : 'card2',
			wizRef : this,
			title : 'Import from FTP-Server.',
			monitorValid : true,
			frame : false,
			fileUpload : true,
			border : false,
			height : '100%',
			folder: this.folder,
			defaults : {
				labelStyle : 'font-size:11px'
			},
			items : [
					{
						border : false,
						bodyStyle : 'background:none;padding-bottom:30px;',
						html : 'Please specify the FTP connection.'
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
						items : [ new Ext.form.TextField({
							id : 'path',
							fieldLabel : 'Folder Name',
							allowBlank : false,
							value: this.folder
						}) ]
					},
					{
						title : 'FTP-Server',
						id : 'fieldset-amazon',
						xtype : 'fieldset',
						autoHeight : true,
						defaults : {
							width : 210,
							labelStyle : 'font-size:11px'
						},
						defaultType : 'textfield',
						items : [ this.ftpModeBox, this.serverField,
								this.userField, this.passwordField ]
					} ]
		});

		// call parent
		MapRed.wizards.ImportDataFtpCard.superclass.initComponent.apply(this,
				arguments);

	},

	// ftp-fields

	setAnonymous : function() {
		this.userField.setVisible(false);
		this.passwordField.setVisible(false);
		this.userField.setValue('');
		this.passwordField.setValue('');
	},

	setStandardLogin : function() {
		this.userField.setVisible(true);
		this.passwordField.setVisible(true);
	}
});
