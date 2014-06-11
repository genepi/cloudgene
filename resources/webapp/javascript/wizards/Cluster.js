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

MapRed.wizards.Cluster = Ext.extend(Ext.ux.Wiz, {

    importDataSource : null,

    importDataCredential : null,

    initComponent : function() {

	Ext.apply(this, {

	    id : 'wizard',
	    previousButtonText : '&lt; Back',
	    title : 'Users',
	    headerConfig : {
		title : 'My Cluster',
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
	    cards : [ new MapRed.wizards.ClusterCard() ],
	    listeners : {
		finish : this.onFinish
	    }

	});

	MapRed.wizards.Cluster.superclass.initComponent.apply(this, arguments);

    },

    onFinish : function() {

	// close wizard
	this.switchDialogState(true);
	this.close();

    }
});

MapRed.wizards.ClusterCard = Ext.extend(Ext.ux.Wiz.Card, {

    serverField : null,

    initComponent : function() {

	Ext.apply(this, {
	    id : 'card2',
	    wizRef : this,
	    title : 'Details about your Apache Hadoop Cluster.',
	    monitorValid : true,
	    frame : false,
	    border : false,
	    height : '100%',
	    defaults : {
		labelStyle : 'font-size:11px'
	    },
	    items : [ {
		title : 'Apache Hadoop Map/Reduce',
		id : 'fieldset-users',
		xtype : 'fieldset',
		autoHeight : true,
		defaults : {
		    // width : 210,
		    anchor : '100%',
		    labelStyle : 'font-size:11px'
		},
		defaultType : 'textfield',
		items : [ new MapRed.wizards.cards.ClusterDetails() ]
	    } ]
	})

	// call parent
	MapRed.wizards.ClusterCard.superclass.initComponent.apply(this,
		arguments);

    }

});
