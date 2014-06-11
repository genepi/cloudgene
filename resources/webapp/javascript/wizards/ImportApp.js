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

Ext.ns('MapRed.wizards');

MapRed.wizards.ImportApp = Ext.extend(Ext.ux.Wiz, {

	importDataSource : null,

	importDataCredential : null,

	initComponent : function() {

		Ext.apply(this, {

			id : 'wizard',
			previousButtonText : '&lt; Back',
			title : 'Install Application',
			headerConfig : {
				title : 'Install Application',
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
			width : 700,
			height : 550,
			cards : [ new MapRed.wizards.ImportAppCard() ],
			listeners : {
				// defined in addSessionHandler
				finish : this.onFinish
			}

		});

		MapRed.wizards.ImportApp.superclass.initComponent
				.apply(this, arguments);

	},

	onFinish : function() {

		/*mycards = this.getWizardData();
		this.showLoadMask(true, 'validating');
		this.switchDialogState(false);
		var values = {};
		var formValues = {};
		for ( var i = 0, len = this.cards.length; i < len; i++) {
			formValues = this.cards[i].form.getValues(false);
			for ( var a in formValues) {
				values[a] = formValues[a];
			}
		}

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
				this.switchDialogState(true);
				this.close();

				// refresh tables
				var taskTable = Ext.getCmp('tasktable');
				var taskStore = taskTable.getStore();
				taskStore.reload();
			}
		});*/
		this.switchDialogState(true);
		this.close();
	}
});

MapRed.wizards.ImportAppCard = Ext.extend(Ext.ux.Wiz.Card, {

	serverField : null,

	initComponent : function() {

		Ext.apply(this, {
			id : 'card2',
			wizRef : this,
			title : 'Install new applications from repository or URL.',
			monitorValid : true,
			frame : false,
			border : false,
			height : '100%',
			defaults : {
				labelStyle : 'font-size:11px'
			},
			items : [{
				title : 'Install from Repository',
				id : 'fieldset-repo',
				xtype : 'fieldset',
				autoScroll:true,
				height: 390,
	
				defaultType : 'textfield',
				items : [ new MapRed.wizards.cards.AppList() ]
			}, {
				title : 'Install from URL',
				id : 'fieldset-url',
				xtype : 'fieldset',
				autoHeight : true,
				hidden: true,
				defaults : {
					width : 210,
					labelStyle : 'font-size:11px'
				},
				defaultType : 'textfield',
				items : [ new Ext.form.TextField({
					id : 'package-url',
					fieldLabel : 'URL',
					value : 'http://',
					allowBlank : false
				}) ]
			} ]
		});

		// call parent
		MapRed.wizards.ImportAppCard.superclass.initComponent.apply(this,
				arguments);

	}
});
