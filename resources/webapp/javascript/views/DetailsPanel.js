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

MapRed.view.DetailsPanel = Ext
	.extend(
		Ext.DataView,
		{

		    initComponent : function() {

			Ext
				.apply(
					this,
					{
					    loadingText : "Loading...",
					    autoScroll : true,
					    overClass : 'x-view-over',
					    itemSelector : 'div.basket-fileblock',
					    emptyText : 'Please click on a job above to see more details.',
					    tpl : this.jobTpl,
					    cls : 'stylePanel',

					    store : new Ext.data.Store(
						    {
							id : 'store-job-details',
							url : 'jobs/details',
							/*listeners : {
							    'load' : function() {

								if (this
									.getCount() > 0) {
								    record = this
									    .getAt(0);

								    if (record
									    .get("state") == 1
									    || record
										    .get("state") == 2
									    || record
										    .get("state") == 3) {

									var t = setTimeout(
										'Ext.StoreMgr.lookup("store-job-details").reload()',
										5000);
								    }
								}
							    }
							},*/
							reader : new Ext.data.JsonReader(
								{
								    fields : [
									    {
										name : 'name',
										type : 'string'
									    },
									    {
										name : 'currentStep',
										type : 'string'
									    },
									    {
										name : 'state',
										type : 'integer'
									    },
									    {
										name : 'startTime',
										type : 'integer'
									    },
									    {
										name : 'endTime',
										type : 'integer'
									    },
									    {
										name : 'executionTime',
										type : 'integer'
									    },
									    {
										id : 'id2',
										name : 'id',
										type : 'name'
									    },
									    {
										name : 'inputParams'
									    },
									    {
										name : 'outputParams'
									    },
									    {
										name : 's3Url',
										type : 'string'
									    } ]
								})
						    })
					});

			// call parent
			MapRed.view.DetailsPanel.superclass.initComponent
				.apply(this, arguments);

			// load store
			this.getStore().load({
			    params : {
				'id' : -1
			    }
			});

		    },

		    jobTpl : new Ext.XTemplate(

			    '<tpl for=".">',
			    '<h2>{name}</h2>{state:this.formatState}',
			    '<div class="box"><h3>Details</h3>',
			    '<table>',
			    /* '<tr><td class="key">Name</td><td class="value">{name}</td></tr>', */
			    '<tr><td class="key">Job-Id</td><td class="value">{id}</td></tr>',
			    '<tr><td class="key">Started At</td><td class="value">{startTime:this.formatTimestamp}</td></tr>',
			    '<tr><td class="key">Finished At</td><td class="value">{endTime:this.formatTimestamp}</td></tr>',
			    '<tr><td class="key">Execution Time</td><td class="value">{executionTime:this.formatTime}</td></tr>',
			    '<tr><td class="key">Logs</td><td class="value">',
			    //'<tpl if="state &gt; 3">',
			    /*'<a href="logs/{id}" target="_blank">View</a>',*/
			    '<a href="JavaScript:showConsole(\'logs/{id}\');">View</a> | <a href="reports/{id}" target="_blank">Statistics</a>',
			    //'</tpl>',
			    '</td></tr></table>',
			    '<tpl if="state == 4">',
			    '<tpl if="outputParams.length != 0">',
			    '<h3>Results</h3>',
			    '<table>',
			    '<tpl for="outputParams">',
			    '<tpl if="download == true">',
			    '<tr>',
			    '<td class="key" style="vertical-align: top">{description}</td><td class="value">',
			    '<table>',
			    '<tpl for="files">',
			    '<tr><td class="value"><a href="results/{parent.jobId}/{path}" target="_blank">{name}</a> ({size})</td></tr>',
			    '</tpl>',
			    '</table>',
			    '</td>',
			    '</tr>',
			    '</tpl>',
			    '</tpl>',
			    '<tpl if="s3Url !=\'\'">',
			    '<tr>',
			    '<td class="key">S3-Url</td><td class="value">{s3Url}</td>',
			    '</tr>',
			    '</tpl>',
			    '</tpl>',
			    '</table>',
			    '</tpl>',
			    '<h3>Arguments</h3>',
			    '<table>',
			    '<tpl for="inputParams">',
			    '<tr><td class="key">{description}</td><td class="value">{value}</td></tr>',
			    '</tpl>',
			    '</table>',
			    '</div></tpl>',
			    {
				formatState : function(value) {

				    if (value == 1) {
					return '<div class="info">The job is waiting in the queue.</div>';
				    } else if (value == 2) {
					return '<div class="info">The job is running.</div>';
				    } else if (value == 3) {
					return '<div class="info">The job is running.</div>';
				    } else if (value == 4) {
					return '<div class="success">The job was executed successfully. The results can be downloaded here.</div>';
				    } else if (value == 6) {
					return '<div class="error">The job was canceled by the user.</div>';
				    } else {
					return '<div class="error">The job execution failed. More information can be found in the logfile.</div>'
				    }
				},

				formatTimestamp : function(unixTimestamp) {

				    if (unixTimestamp > 0) {
					var dt = new Date(unixTimestamp);
					// return dt.toLocaleString();
					return dateFormat(dt, "default");
				    } else {
					return '-';
				    }

				},

				formatTime : function(value) {

				    if (value == 0) {

					return '-';

				    } else {

					var h = (Math
						.floor((value / 1000) / 60 / 60));
					var m = ((Math
						.floor((value / 1000) / 60)) % 60);

					return (h > 0 ? h + ' h ' : '')
						+ (m > 0 ? m + ' min ' : '')
						+ ((Math.floor(value / 1000)) % 60)
						+ ' sec';

				    }
				}

			    })
		});

Ext.reg('detailspanel', MapRed.view.DetailsPanel);


showConsole = function(file){
    
    var temp = new MapRed.dialogs.consoleWindow({logFile: file});
    temp.show();
    
}
