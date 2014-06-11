Ext.ns('MapRed.dialogs');

MapRed.dialogs.PasswordDialog = Ext.extend(Ext.Window, {

    initComponent : function() {

	Ext.apply(this, {

	    title : 'User Password',
	    width : 400,
	    height : 200,
	    shadowOffset : 6,
	    buttonAlign : 'center',
	    layout : 'fit',
	    id : 'window-password-dialog',

	    items : [ {
		buttonAlign : 'center',
		defaultType : 'textfield',
		monitorValid : true,
		border : 0,
		url : '../updateUserPassword',
		defaults : {
		    anchor : '100%',
		    labelStyle : 'font-size:11px'
		},
		bodyStyle : 'padding: 5px;',
		xtype : 'form',
		id : 'form-password-dialog',
		items : [ {
		    id : 'fieldset-password',
		    xtype : 'fieldset',
		    autoHeight : true,
		    defaults : {
			width : 210,
			labelStyle : 'font-size:11px'
		    },
		    defaultType : 'textfield',
		    items : [ new Ext.form.TextField({
			id : 'old-password',
			fieldLabel : 'Old Password',
			value : '',
			allowBlank : true,
			inputType : 'password'
		    }), new Ext.form.TextField({
			id : 'new-password',
			fieldLabel : 'New Password',
			value : '',
			allowBlank : true,
			inputType : 'password'
		    }), new Ext.form.TextField({
			id : 'confirm-new-password',
			fieldLabel : 'Confirm New Password',
			value : '',
			allowBlank : true,
			inputType : 'password'
		    }) ]
		} ]
	    } ],

	    buttons : [ {
		text : 'OK',
		id : 'ok-button',
		formBind : true,
		handler : this.onOkay,
		scope : this
	    }, {
		text : 'Cancel',
		id : 'close-button',
		handler : this.onClose,
		scope : this
	    } ]

	});

	MapRed.dialogs.PasswordDialog.superclass.initComponent.call(this);

    },

    onClose : function() {

	this.close();

    },

    onOkay : function() {

	var window = this;

	Ext.getCmp('form-password-dialog').getForm().submit({
	    method : 'POST',
	    waitTitle : 'Connecting',
	    waitMsg : 'Sending data...',

	    success : function(form, action) {

		window.close();

		Ext.Msg.show({
		    title : 'Successfully updated',
		    msg : "Your password was successfully updated.",
		    buttons : Ext.Msg.OK
		});

	    },

	    failure : function(form, action) {

		Ext.Msg.show({
		    title : 'Password update failed',
		    msg : "Please check your password.",
		    buttons : Ext.Msg.OK
		});

	    }
	});

    }
});