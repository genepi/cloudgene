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

function openSignup(){
    
    Ext.Msg.alert('Sign Up', 'Please send an email to sebastian.schoenherr@uibk.ac.at to receive the login data.');
    
}

Ext.onReady(function(){
    Ext.QuickTips.init();
    
    // Create a variable to hold our EXT Form Panel. 
    // Assign various config options as seen.	 
    var login = new Ext.FormPanel({
        labelWidth: 80,
        url: '../login',
        frame: true,
        title: 'Please Login',
        defaultType: 'textfield',
        monitorValid: true,
        bodyStyle:	'padding: 5px;',
        // Specific attributes for the text fields for username / password. 
        // The "name" attribute defines the name of variables sent to the server.
        items: [{
            value: '',
            fieldLabel: 'Username',
            name: 'loginUsername',
            allowBlank: false
        }, {
            value: '',
            fieldLabel: 'Password',
            name: 'loginPassword',
            inputType: 'password',
            allowBlank: false
        },{
		xtype : 'panel',
		border : false,
		anchor : '100%',
		html : '<p style="text-align:left;padding-top:10px;">Please send an email to <a href="mailto:sebastian.schoenherr@uibk.ac.at">sebastian.schoenherr@uibk.ac.at</a> to receive the login data.<p>'
	}],
        
        // All the magic happens after the user clicks the button     
        buttons: [{
            text: 'Login',
            formBind: true,
            // Function that fires when user clicks the button 
            handler: function(){
                login.getForm().submit({
                    method: 'POST',
                    waitTitle: 'Connecting',
                    waitMsg: 'Sending data...',
                    
                    success: function(form, action){
                        var obj = Ext.util.JSON.decode(action.response.responseText);
                        if (obj.success) {
                        
                            var redirect = 'start.html';
                            window.location = redirect + window.location.hash;
                            console.log(redirect + window.location.hash);
                        }
                        else {
                            Ext.Msg.alert('Login Failed', obj.message);
                            login.getForm().reset();
                        }
                        
                    },
                    
                    failure: function(form, action){
                        var obj = Ext.util.JSON.decode(action.response.responseText);
                        Ext.Msg.alert('Login Failed', obj.message);
                        login.getForm().reset();
                    }
                });
            }
        }],
        keys: [{
            key: [Ext.EventObject.ENTER],
            handler: function(){
            
                login.getForm().submit({
                    method: 'POST',
                    waitTitle: 'Connecting',
                    waitMsg: 'Sending data...',
                    
                    success: function(form, action){
                        var obj = Ext.util.JSON.decode(action.response.responseText);
                        if (obj.success) {
                        
                            var redirect = 'start.html';
                            window.location = redirect + window.location.hash;
                            console.log(redirect + window.location.hash);
                        }
                        else {
                            Ext.Msg.alert('Login Failed', obj.message);
                            login.getForm().reset();
                        }
                        
                    },
                    
                    failure: function(form, action){
                        var obj = Ext.util.JSON.decode(action.response.responseText);
                        Ext.Msg.alert('Login Failed', obj.message);
                        login.getForm().reset();
                    }
                });
                
                
            }
        }]
    });
    
    
    // This just creates a window to wrap the login form. 
    // The login object is passed to the items collection.       
    var win = new Ext.Window({
        layout: 'fit',
        width: 320,
        height: 180,
        closable: false,
        resizable: false,
        plain: true,
        border: false,
        items: [login]
    });
    win.show();
	
});
