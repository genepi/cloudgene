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

Ext.ns('MapRed.wizards.cards');

MapRed.wizards.cards.AppList = Ext
	.extend(
		Ext.DataView,
		{

		    initComponent : function() {

			Ext.apply(this, {
			    loadingText : "Loading Apps...",
			    autoScroll : true,
			    overClass : 'x-view-over',
			    itemSelector : 'div.basket-fileblock',
			    emptyText : 'No apps found...',
			    tpl : this.jobTpl,
			    cls : 'applicationPanel',

			    store : new Ext.data.Store({

				autoLoad : true,

				url : 'getAppsFromRepo',

				reader : new Ext.data.JsonReader({
				    fields : [ {
					name : 'name',
					type : 'string'
				    }, {
					name : 'description',
					type : 'string'
				    }, {
					name : 'website',
					type : 'string'
				    }, {
					name : 'author',
					type : 'string'
				    }, {
					name : 'source',
					type : 'string'
				    }, {
					name : 'version',
					type : 'string'
				    } ]
				})
			    })
			});

			// call parent
			MapRed.wizards.cards.AppList.superclass.initComponent
				.apply(this, arguments);

		    },

		    jobTpl : new Ext.XTemplate(

			    '<tpl for=".">',
			    '<div class="application-item">',
			    '<h2>{name}</h2>',
			    '<p>{description}</p></br><table width="100%">',
			    '<tr><td><p>by {author} <tpl if="version"> | Version: {version} </tpl> <tpl if="website"> | <a href="{website}" target="_blank" hint="{website}">Website</a> </tpl></p></td>',
			    '<td><p style="text-align: right"><input onclick="installApp(\'{source}\')" class="x-btn-text" type="button" id="delete-button" value="Install" width="50px" /></p></td></tr></table>',
			    '</div></tpl><div class="x-clear"></div>')
		});

Ext.reg('applist', MapRed.wizards.cards.AppList);

function installApp(url) {

    var values = {};
    values['package-url'] = url;

    Ext.Ajax.request({
	url : '../installApp',

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
	    this.close();

	    // reload tasks
	    var jobTable = Ext.getCmp('jobtable');
	    var storeJobs = jobTable.getStore();
	    storeJobs.reload();
	}
    });

}
