/*******************************************************************************
 * Cloudgene: A graphical MapReduce interface for cloud computing
 * 
 * Copyright (C) 2010, 2011 Sebastian Schoenherr, Lukas Forer
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

Ext.ns('MapRed.wizards');

MapRed.wizards.AccountSettings = Ext.extend(Ext.ux.Wiz, {

    importDataSource : null,

    importDataCredential : null,

    initComponent : function() {

	Ext.apply(this, {

	    id : 'wizard',
	    previousButtonText : '&lt; Back',
	    title : 'My Account',
	    headerConfig : {
		title : 'MyAccount',
		image : '../images/settings.png',
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
	    cards : [ new MapRed.wizards.AccountSettingsCard() ],
	    listeners : {
		finish : this.onFinish
	    }

	});

	MapRed.wizards.AccountSettings.superclass.initComponent.apply(this,
		arguments);

    },

    onFinish : function() {

	var values = {};
	var formValues = {};

	// read all general params
	for ( var i = 0, len = this.cards.length; i < len; i++) {
	    formValues = this.cards[i].form.getValues(false);
	    for ( var a in formValues) {
		values[a] = formValues[a];
	    }
	}

	Ext.Ajax.request({
	    url : '../updateUserSettings',
	    method : 'POST',
	    scope : this,
	    jsonData : values,

	    failure : function(response, request) {

		// error
		Ext.Msg.show({
		    title : 'Exception',
		    msg : response.responseText,
		    buttons : Ext.Msg.OKCANCEL
		});

		// close wizard
		this.switchDialogState(true);
		this.close();
	    },

	    success : function(response, request) {

		// successfully added
		Ext.Msg.show({
		    title : 'Successfully updated!',
		    msg : "Your settings were successfully updated.",
		    buttons : Ext.Msg.OK
		});

		// close wizard
		this.switchDialogState(true);
		this.close();

	    }
	});

    }
});

MapRed.wizards.AccountSettingsCard = Ext.extend(Ext.ux.Wiz.Card, {

    serverField : null,

    initComponent : function() {

	Ext.apply(this, {
	    id : 'card2',
	    wizRef : this,
	    title : 'Change here your account settings.',
	    monitorValid : true,
	    frame : false,
	    border : false,
	    height : '100%',
	    onCardShow : this.loadSettings,
	    defaults : {
		labelStyle : 'font-size:11px'
	    },
	    items : [ {
		title : 'General',
		id : 'fieldset-general',
		xtype : 'fieldset',
		autoHeight : true,
		defaults : {
		    width : 210
		},
		defaultType : 'textfield',
		items : [ new Ext.form.TextField({
		    id : 'full-name',
		    fieldLabel : 'Full Name',
		    value : '',
		    allowBlank : true
		}),new Ext.form.TextField({
		    id : 'mail',
		    fieldLabel : 'E-Mail Address',
		    value : '',
		    allowBlank : true
		}) ]
	    }, {
		title : 'Amazon AWS Credentials',
		id : 'fieldset-aws-credentials',
		xtype : 'fieldset',
		autoHeight : true,
		defaults : {
		    width : 210
		},
		defaultType : 'textfield',
		items : [ {
		    hideLabel  : true,
		    xtype : 'checkbox',
		    style : 'font-size:11px',
		    boxLabel : 'Save my AWS-Credentials',
		    id : 'save-keys',
		    name : 'save-keys',
		    listeners : {
			'check' : function(checkbox, checked) {
			    if (checked) {
				Ext.getCmp("aws-key").enable();
				Ext.getCmp("aws-secret-key").enable();
			    } else {
				Ext.getCmp("aws-key").disable();
				Ext.getCmp("aws-secret-key").disable();
			    }
			}
		    }

		}, new Ext.form.TextField({
		    id : 'aws-key',
		    fieldLabel : 'AWS Key',
		    value : '',
		    allowBlank : true
		}), new Ext.form.TextField({
		    id : 'aws-secret-key',
		    fieldLabel : 'AWS Secret Key',
		    value : '',
		    allowBlank : true,
		    inputType : 'password'
		}) ]
	    }, {
		title : 'Export to S3 Bucket',
		id : 'fieldset-s3-export',
		xtype : 'fieldset',
		autoHeight : true,
		defaults : {
		    width : 210

		},
		defaultType : 'textfield',
		items : [ {
		    hideLabel  : true,
		    xtype : 'checkbox',
		    boxLabel : 'Export all results automatically to Amazon S3',
		    id : 'export-to-s3',
		    name : 'export-to-s3',
		    width : 300,
		    listeners : {
			'check' : function(checkbox, checked) {
			    if (checked) {
				Ext.getCmp("s3-bucket").enable();
				Ext.getCmp("export-input-to-s3").enable();
			    } else {
				Ext.getCmp("s3-bucket").disable();
				Ext.getCmp("export-input-to-s3").disable();
			    }
			}
		    }

		}, new Ext.form.TextField({
		    id : 's3-bucket',
		    fieldLabel : 'S3-Bucket',
		    value : '',
		    allowBlank : true
		}),
		{
		    hideLabel  : true,
		    xtype : 'checkbox',
		    boxLabel : 'Export input data',
		    id : 'export-input-to-s3',
		    name : 'export-input-to-s3',
		    width : 300

		}]
	    },{
		
		xtype: 'button',
		text : 'Change Password...',
		handler : this.showPasswordDialog,
		scope : this				
	    } ]
	});

	// call parent
	MapRed.wizards.AccountSettingsCard.superclass.initComponent.apply(this,
		arguments);

    },
    
    showPasswordDialog : function() {
	
	var dialog = new MapRed.dialogs.PasswordDialog();
	dialog.show();

    },
    

    loadSettings : function() {

	// general tool informations
	Ext.Ajax.request({
	    url : '../users/details',
	    success : function(response) {

		arr = Ext.util.JSON.decode(response.responseText);

		// AWS Credentials
		if (arr.saveCredentials == true) {

		    Ext.getCmp('save-keys').setValue(true);
		    Ext.getCmp('aws-key').setValue(arr.awsKey);
		    Ext.getCmp('aws-secret-key').setValue(arr.awsSecretKey);
		    Ext.getCmp("aws-key").enable();
		    Ext.getCmp("aws-secret-key").enable();

		} else {

		    Ext.getCmp('aws-key').setValue("");
		    Ext.getCmp('aws-secret-key').setValue("");
		    Ext.getCmp("aws-key").disable();
		    Ext.getCmp("aws-secret-key").disable();

		}
		
		// Export To S3
		if (arr.exportToS3 == true) {

		    Ext.getCmp('export-to-s3').setValue(true);
		    Ext.getCmp('s3-bucket').setValue(arr.s3Bucket);
		    Ext.getCmp('export-input-to-s3').setValue(arr.exportInputToS3);
		    
		} else {

		    Ext.getCmp('export-input-to-s3').disable();
		    Ext.getCmp('export-input-to-s3').setValue(false);
		    Ext.getCmp('s3-bucket').setValue("");
		    Ext.getCmp('s3-bucket').disable();

		}

		// General Information
		Ext.getCmp('full-name').setValue(arr.fullName);
		Ext.getCmp('mail').setValue(arr.mail);
	    }
	});

	if (this.monitorValid) {
	    this.startMonitoring();
	}

    }

});
