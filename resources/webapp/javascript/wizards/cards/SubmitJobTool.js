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

MapRed.wizards.SubmitJobTool = Ext
	.extend(
		Ext.ux.Wiz.Card,
		{

		    initComponent : function() {

			Ext
				.apply(
					this,
					{
					    id : 'card1',
					    wizRef : this,
					    title : 'Select an Application.',
					    monitorValid : true,
					    border : false,
					    items : [

						    {
							border : false,
							bodyStyle : 'background:none; padding-top: 5px; padding-bottom: 10px;',
							html : '<p>Chose the application you want to execute:</p>'
						    },

						    new Ext.form.TextField({
							id : "hidden-field",
							name : 'hidden-field',
							value : '',
							fieldLabel : 'Folder',
							allowBlank : false,
							readOnly : true,
							hidden : true
						    }),

						    new Ext.tree.TreePanel(
							    {
								id : 'toolsTree',
								useArrows : true,
								autoScroll : true,
								animate : false,
								enableDD : false,
								containerScroll : true,
								allowBlank : false,

								anchor : '100%',
								height : 230,
								// autoHeight :
								// false,
								border : false,

								loader : new Ext.tree.TreeLoader(
									{
									    url : '../apps',
									    requestMethod : 'GET',
									    preloadChildren : true,
									    autoLoad : true
									}),

								root : new Ext.tree.TreeNode(
									{
									    id : 'root2',
									    text : 'Applications',
									    expanded : true
									}),

								viewConfig : {
								    forceFit : true
								},

								listeners : {
								    click : {
									fn : this.clickListener
								    }
								}

							    }),

						    new Ext.Panel(
							    {
								id : "help-text",
								name : 'help-text',
								value : '',
								hideLabel : true,
								allowBlank : true,
								readOnly : true,
								anchor : '100%',
								bodyStyle : 'padding: 5px;',
								hidden : true
							    }),

					    ]
					});

			MapRed.wizards.SubmitJobTool.superclass.initComponent
				.apply(this, arguments);
		    },

		    clickListener : function(node, event) {

			if (node.isLeaf()) {
			    Ext.getCmp('hidden-field').setValue(
				    "selction-not-empty");
			    Ext.getCmp('help-text').update(
				    "<p><b>" + node.attributes.name + "</b></p><p style=\"padding-top:5px;\">"
					    + node.attributes.description
					    + "</p><p style=\"padding-top:5px;\"> by "+ node.attributes.author + " | Version " + node.attributes.version+" | <a href=\"" + node.attributes.website +"\">Website</a></p>");
			    Ext.getCmp('help-text').show();
			} else {
			    Ext.getCmp('hidden-field').setValue("");
			    Ext.getCmp('help-text').update("");
			    Ext.getCmp('help-text').hide();
			}
		    }

		});