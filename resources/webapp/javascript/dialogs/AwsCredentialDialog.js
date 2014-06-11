Ext.ns('MapRed.dialogs');

MapRed.dialogs.AwsCredentialDialog = Ext.extend(Ext.Window, {

    store : null,

    privateRadioButton : null,

    initComponent : function() {

	Ext.apply(this, {

	    title : 'AWS Credentials',
	    width : 400,
	    height : 230,
	    shadowOffset : 6,
	    buttonAlign : 'center',
	    layout : 'fit',
	    id : 'window-aws-dialog',

	    items : [ {
		buttonAlign : 'center',
		defaultType : 'textfield',
		monitorValid : true,
		url : '../updateCredentials',
		defaults : {
		    width : 210,
		    // anchor: '100%',
		    labelStyle : 'font-size:11px'
		},
		bodyStyle : 'padding: 10px;',
		xtype : 'form',
		id : 'form-aws-dialog',
		items : [  {
		    value : '',
		    fieldLabel : 'AWS Key',
		    name : 'aws-key',
		    allowBlank : false
		}, {
		    value : '',
		    fieldLabel : 'AWS Secret Key',
		    name : 'aws-secret-key',
		    inputType : 'password',
		    allowBlank : false
		}, {
		    hideLabel : true,
		    xtype : 'checkbox',
		    style : 'font-size:11px',
		    boxLabel : 'Save my AWS-Credentials',
		    name : 'save-keys'

		},{
		    xtype : 'box',
		    border : false,
		    style : 'font-size:12px; padding-top: 20px;',
		    bodyStyle : 'background:none;padding-bottom:30px;',
		    html : 'If you want to import ony public buckets, please ignore this messagebox and click Cancel.',
		    anchor: '100%'
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

	MapRed.dialogs.AwsCredentialDialog.superclass.initComponent.call(this);

    },

    onClose : function() {
	this.privateRadioButton.disable();
	this.close();

    },

    onOkay : function() {

	var window = this;

	Ext.getCmp('form-aws-dialog').getForm().submit({
	    method : 'POST',
	    waitTitle : 'Connecting',
	    waitMsg : 'Sending data...',

	    success : function(form, action) {
		// Ext.Msg.alert('Login Failed', "OK");
		window.store.reload();
		window.close();
	    },

	    failure : function(form, action) {
		Ext.Msg.alert('Login Failed', "ERROR");
		// if (window.privateRadioButton != null){
		// }
		window.privateRadioButton.disable();
		window.close();
	    }
	});

    }
});