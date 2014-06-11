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

Ext.ns('MapRed.view');

MapRed.view.Toolbar = Ext.extend(Ext.Toolbar, {
    initComponent : function() {

	Ext.apply(this, {

	    height : 65,
	    collapsible : false,
	    split : false,
	    border : false,
	    bodyStyle : 'background-color: #ffffff',
	    items : [ {
		xtype : 'tbspacer'
	    },	    {
		xtype : 'tbfill'
	    }, {
		text : 'My Account',
		tooltip : 'My Account Settings',
		xtype : 'tbbutton',
		icon : '../images/user.png',
		cls : 'x-btn-text-icon',
		handler : function(btn) {

		    var wizard = new MapRed.wizards.AccountSettings();
		    wizard.show();

		}
	    }, {
		text : 'My Cluster',
		tooltip : 'My Cluster Information',
		xtype : 'tbbutton',
		icon : '../images/gear.png',
		cls : 'x-btn-text-icon',
		handler : function(btn) {

		    var wizard = new MapRed.wizards.Cluster();
		    wizard.show();

		}

	    }, {
		id: 'admin-button',
		text : 'Admin',
		tooltip : 'Users',		
		xtype : 'tbbutton',
		icon : '../images/icons/cog.png',
		cls : 'x-btn-text-icon',
		hidden : true,
		handler : function(btn) {

		    var wizard = new MapRed.wizards.Users();
		    wizard.show();

		}
	    }, {
		text : 'Logout',
		xtype : 'tbbutton',
		icon : '../images/logout.png',
		iconAlign : 'top',
		cls : 'x-btn-text-icon',
		scale : 'large',
		handler : function(btn) {
		    Ext.Msg.confirm('Logout', 'Really?', function(btn, text) {
			if (btn == 'yes') {
			    window.location = "logout";
			}
		    });
		}
	    } ]

	});

	// call parent
	MapRed.view.Toolbar.superclass.initComponent.apply(this, arguments);
    }
});

Ext.reg('mytoolbar', MapRed.view.Toolbar);