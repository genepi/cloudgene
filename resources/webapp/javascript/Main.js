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

Ext
	.onReady(function() {

	    Ext.QuickTips.init();

	    var temp = null;

	    var viewport = new Ext.Viewport(
		    {
			layout : 'border',
			id : 'viewport',
			items : [
				{
				    xtype : 'box',
				    region : 'north',
				    applyTo : 'header',
				    height : 59
				},
				{
				    xtype : 'mainpanel',
				    region : 'center',
				    layout : 'fit'
				},
				{
				    xtype : 'box',
				    region : 'south',
				    html : '<div class="footer" id="footer"><a href="http://cloudgene.uibk.ac.at">Cloudgene Website</a> | <a href="mailto:sebastian.schoenherr@uibk.ac.at">Contact</a> | <a href="http://cloudgene.uibk.ac.at/team.html">Team</a> | <a href="http://genepi.i-med.ac.at">Genepi Innsbruck</a> | <a href="http://dbis-informatik.uibk.ac.at">DBIS</a> </div>',
				    height : 25,
				    border : false
				} ]
		    });

	    // get tables and jobs
	    var jobTable = Ext.getCmp('jobtable');
	    var storeJobs = jobTable.getStore();

	    var detailsPanel = Ext.getCmp('detailspanel');
	    var detailsStore = detailsPanel.getStore();

	    // get user information
	    Ext.Ajax.request({
		url : '../users/details',
		success : function(response) {

		    arr = Ext.util.JSON.decode(response.responseText);
		    Ext.getCmp('admin-button').setVisible(arr.role == 'Admin');

		}
	    });

	    // register master-detail handler

	    jobTable.getSelectionModel().on('rowselect',
		    function(sm, rowIdx, r) {
			detailsStore.load({
			    params : {
				'id' : r.data.id
			    }
			});
		    });

	    function refreshJobs() {

		storeJobs.reload();
	    }

	    refreshJobs();
 		Ext.History.init(function(token){
			
			if (Ext.History.getToken() != null && Ext.History.getToken() != 'null' ){
			
				var wizard = new MapRed.wizards.SubmitJob({tool: Ext.History.getToken()});
		    	wizard.show(); 
		    }       
        
    	});
	});
