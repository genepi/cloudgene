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

MapRed.wizards.Users = Ext.extend(Ext.ux.Wiz, {

    importDataSource: null,

    importDataCredential: null,

    initComponent: function() {

        Ext.apply(this, {

            id: 'wizard',
            previousButtonText: '&lt; Back',
            title: 'Users',
            headerConfig: {
                title: 'Users',
                image: '../images/settings.png',
                stepText: '{2}'
            },
            loadMaskConfig: {
                'default': 'Please wait, validating input...'
            },
            cardPanelConfig: {
                defaults: {

                    bodyStyle: 'padding:13px;background-color:#ffffff;',
                    border: false

                }
            },
            width: 480,
            height: 500,
            cards: [new MapRed.wizards.UsersCard()],
            listeners: {
                finish: this.onFinish
            }

        });

        MapRed.wizards.Users.superclass.initComponent.apply(this,
        arguments);

    },

    onFinish: function() {

        // close wizard
        this.switchDialogState(true);
        this.close();

    }
});



MapRed.wizards.UsersCard = Ext.extend(Ext.ux.Wiz.Card, {

    serverField: null,

    initComponent: function() {

        Ext.apply(this, {
            id: 'card2',
            wizRef: this,
            title: 'Manage users here.',
            monitorValid: true,
            frame: false,
            border: false,
            height: '100%',
            defaults: {
                labelStyle: 'font-size:11px'
            },
            items: [{
                title: 'Users',
                id: 'fieldset-users',
                xtype: 'fieldset',
                autoHeight: true,
                defaults: {
                    //width : 210,
                    anchor: '100%',
                    labelStyle: 'font-size:11px'
                },
                defaultType: 'textfield',
                items: [new Ext.grid.GridPanel({
                    id: 'users-grid',
                    hideLabel: true,
                    value: '',
                    allowBlank: true,
                    height: 240,
                    columns: [{
                        id: 'name',
                        header: "Username",
                        width: 110,
                        flex: 1,
                        dataIndex: 'username'
                    },
                    {
                        header: 'Full Name',
                        dataIndex: 'fullName',
                        flex: 1,
                        width: 170,
                        align: 'center'
                    },
                    {
                        header: 'E-Mail',
                        dataIndex: 'mail',
                        flex: 1,
                        width: 170,
                        align: 'center'
                    },
                    {
                        header: 'Role',
                        dataIndex: 'role',
                        flex: 1,
                        width: 170,
                        align: 'center'
                    }],

                    store: new Ext.data.Store(
                    {

                        autoDestroy: true,
                        url: '../users',
                        autoLoad: true,

                        reader: new Ext.data.JsonReader({
                            fields: [{
                                name: 'username',
                                type: 'string'
                            },
                            {
                                name: 'fullName',
                                type: 'string'
                            },
                            {
                                name: 'id',
                                type: 'integer'
                            },
                            {
                                name: 'mail',
                                type: 'string'
                            },
                            {
                                name: 'role',
                                type: 'string'
                            }]
                        })


                    })

                }), new Ext.Button({
                    text: 'New User',
                    handler: function() {
                        var dialog = new MapRed.dialogs.UserDialog({
                        	url : '../users/new',
                            onSuccess : function(){
                            	Ext.getCmp('users-grid').getStore().reload();
                            }                        	
                        });
                        dialog.show();
                    }
                }), new Ext.Button({
                    text: 'Edit User',
                    handler: function() {

                        selectedUser = Ext.getCmp('users-grid').getSelectionModel().getSelections()[0];

                        var dialog = new MapRed.dialogs.UserDialog({
                        	url : '../users/update',
                            userId: selectedUser.get("id"),
                            username: selectedUser.get('username'),
                            role: selectedUser.get('role'),
                            onSuccess : function(){
                            	Ext.getCmp('users-grid').getStore().reload();
                            }
                        });
                        dialog.show();
                    }
                }), new Ext.Button({
                    text: 'Delete User',
                    handler: function() {

                        selectedUser = Ext.getCmp('users-grid').getSelectionModel().getSelections()[0];

                        Ext.Msg.confirm('Delete User', 'Do you really want to delete user "' +Ext.getCmp('users-grid').getSelectionModel().getSelections()[0].get('username')+'"?',
                        function(btn, text) {
                            if (btn == 'yes') {
                                Ext.Ajax.request({
                                    url: '../users/delete',
                                    params: {
                                        id: Ext.getCmp('users-grid').getSelectionModel().getSelections()[0].get('id')
                                    },
                                    success: function(response) {
										Ext.getCmp('users-grid').getStore().reload();
                                    }
                                });
                            }
                        });
                    }
                })]
            }]
        })

        // call parent
        MapRed.wizards.UsersCard.superclass.initComponent.apply(this,
        arguments);

    }

});
