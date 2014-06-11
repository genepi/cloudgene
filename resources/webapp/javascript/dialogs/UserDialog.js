Ext.ns('MapRed.dialogs');

MapRed.dialogs.UserDialog = Ext.extend(Ext.Window, {

	url : '../users/new',

    userId: '',

    username: '',

    role: 'User',

    initComponent: function() {

        Ext.apply(this, {

            title: 'User',
            width: 400,
            height: 200,
            shadowOffset: 6,
            buttonAlign: 'center',
            layout: 'fit',
            id: 'window-user-dialog',

            items: [{
                buttonAlign: 'center',
                defaultType: 'textfield',
                monitorValid: true,
                border: 0,
                url: this.url,
                defaults: {
                    anchor: '100%',
                    labelStyle: 'font-size:11px'
                },
                bodyStyle: 'padding: 5px;',
                xtype: 'form',
                id: 'form-user-dialog',
                items: [{
                    id: 'fieldset-user',
                    xtype: 'fieldset',
                    autoHeight: true,
                    defaults: {
                        width: 210,
                        labelStyle: 'font-size:11px'
                    },
                    defaultType: 'textfield',
                    items: [new Ext.form.TextField({
                        id: 'userid',
                        fieldLabel: 'User-id',
                        value: this.userId,
                        allowBlank: true,
                        hidden: true
                    }), new Ext.form.TextField({
                        id: 'username',
                        fieldLabel: 'Username',
                        value: this.username,
                        allowBlank: true
                    }), new Ext.form.ComboBox({
                        id: 'role-combo',
                        name: 'role-combo',
                        store: new Ext.data.SimpleStore({
                            fields: ['key', 'value'],
                            data: [['Admin', 'Admin'], ['User', 'User']]
                        }),
                        value: this.role,
                        displayField: 'value',
                        valueField: 'key',
                        typeAhead: false,
                        mode: 'local',
                        triggerAction: 'all',
                        emptyText: 'Select a value...',
                        fieldLabel: 'Role',
                        selectOnFocus: true,
                        editable: false,
                        allowBlank: false
                    }), new Ext.form.TextField({
                        id: 'new-password',
                        fieldLabel: 'Password',
                        value: '',
                        allowBlank: true,
                        inputType: 'password'
                    }), new Ext.form.TextField({
                        id: 'confirm-new-password',
                        fieldLabel: 'Confirm Password',
                        value: '',
                        allowBlank: true,
                        inputType: 'password'
                    })]
                }]
            }],

            buttons: [{
                text: 'OK',
                id: 'ok-button',
                formBind: true,
                handler: this.onOkay,
                scope: this
            },
            {
                text: 'Cancel',
                id: 'close-button',
                handler: this.onClose,
                scope: this
            }]

        });

        MapRed.dialogs.UserDialog.superclass.initComponent.call(this);

    },

    onClose: function() {

        this.close();

    },

	onSuccess: function (){
	},

    onOkay: function() {

        var window = this;

        Ext.getCmp('form-user-dialog').getForm().submit({
            method: 'POST',
            waitTitle: 'Connecting',
            waitMsg: 'Sending data...',

            success: function(form, action) {

                window.close();

                Ext.Msg.show({
                    title: 'Successfully updated',
                    msg: "Your password was successfully updated.",
                    buttons: Ext.Msg.OK
                });
                
                window.onSuccess();

            },

            failure: function(form, action) {

                Ext.Msg.show({
                    title: 'Password update failed',
                    msg: "Please check your password.",
                    buttons: Ext.Msg.OK
                });

            }
        });

    }
});