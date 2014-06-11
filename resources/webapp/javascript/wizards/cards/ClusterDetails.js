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

MapRed.wizards.cards.ClusterDetails = Ext.extend(Ext.DataView, {

    initComponent : function() {

	Ext.apply(this, {
	    loadingText : "Loading Cluster Details...",
	    autoScroll : true,
	    bodyStyle : {
		padding : '7px'
	    },
	    overClass : 'x-view-over',
	    itemSelector : 'div.basket-fileblock',
	    emptyText : 'Details not found...',
	    tpl : this.jobTpl,
	    // cls : 'stylePanel',

	    store : new Ext.data.Store({

		autoLoad : true,

		url : 'cluster',

		reader : new Ext.data.JsonReader({
		    fields : [ {
			name : 'date'
		    }, {
			name : 'version'
		    }, {
			name : 'status'
		    } ]
		})
	    })
	});

	// call parent
	MapRed.wizards.cards.AppList.superclass.initComponent.apply(this,
		arguments);

    },

    jobTpl : new Ext.XTemplate(

    '<tpl for=".">', '<p><b>Version: </b> {version}</p>',
	    '<p><b>Date: </b> {date}</p>', '<tpl for="status">',
	    '<p><b>State:</b> {jobTrackerState}</p>',
	    '<p><b>Datanodes:</b> {taskTrackers}</p>',
	    '<p><b>Machines:</b><br>',
	    '<tpl for="activeTrackerNames">',
	    '{.}<br>',
	    '</p>',
	    '</tpl>',
	    '<p><b>Blacklisted Datanodes:</b> {blacklistedTrackers}</p>',
	    '<p><b>Max Reduce Tasks:</b> {maxReduceTasks}</p>',
	    '<p><b>Running Map Tasks:</b> {mapTasks}</p>',
	    '<p><b>Running Reduce Tasks:</b> {reduceTasks}</p>',
	    '</tpl></tpl><div class="x-clear"></div>')
});

Ext.reg('applist', MapRed.wizards.cards.ClusterDetails);