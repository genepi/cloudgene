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

MapRed.wizards.SubmitJobParameters = Ext.extend(Ext.ux.Wiz.Card,
	{

	    initComponent : function() {

		Ext.apply(this, {
		    id : 'card2',
		    wizRef : this,
		    title : 'Set all parameters.',
		    monitorValid : true,
		    border : false,
		    layout : 'form',
		    frame : false,
		    autoScroll : true,
		    defaults : {
			labelStyle : 'font-size:11px'
		    },

		    onCardShow : this.buildInterface,

		    items : [ {
			title : 'General',
			id : 'general-params',
			xtype : 'fieldset',
			autoHeight : true,
			defaults : {
			    width : 250
			},
			defaultType : 'textfield',
			labelWidth : 120,
			items : [ {
			    id : 'job-name',
			    name : 'job-name',
			    fieldLabel : 'Job-Name',
			    value : 'job-xxx',
			    allowBlank : false
			}, {
			    id : 'job-id',
			    name : 'job-id',
			    fieldLabel : 'Id',
			    value : 'job-xxx',
			    allowBlank : true,
			    hidden : true
			} ]
		    }, {
			title : 'Input Parameters',
			id : 'input-params',
			xtype : 'fieldset',
			autoHeight : true,
			labelWidth : 120,
			defaults : {
			    width : 250
			}
		    } ]
		});

		// call parent
		MapRed.wizards.SubmitJobParameters.superclass.initComponent
			.apply(this, arguments);
	    },

	    // build the dynamical interface
	    buildInterface : function() {

		if (this.mytool == null){

			tool = Ext.getCmp('toolsTree').getSelectionModel()
				.getSelectedNode().id;
			
		}else{
		
			tool = this.mytool;
			console.log(this.mytool);
		
		}

		// general tool informations
		Ext.Ajax.request({
		    url : '../apps/details',
		    params : {
			tool : tool
		    },
		    success : function(response) {

			arr = Ext.util.JSON.decode(response.responseText);
			Ext.getCmp('job-name').setValue(arr[0].jobName);
			Ext.getCmp('job-id').setValue(arr[0].jobName);
		    }
		});

		// parameters
		Ext.Ajax.request({
		    url : '../apps/params',
		    params : {
			tool : tool
		    },
		    success : this.buildParamPanel
		});

		// start monitoring
		if (this.monitorValid) {
		    this.startMonitoring();
		}

	    },

	    // builds input fields based on parameter descriptiob
	    buildParamPanel : function(response) {

		var params = Ext.util.JSON.decode(response.responseText);

		var inputParams = Ext.getCmp('input-params');
		inputParams.removeAll();

		for (i = 0; i < params.length; i++) {

		    var inputField = null;
		    var param = params[i];

		    if (param.type == "hdfs-folder"
			    || param.type == "hdfs-file") {

			// Hdfs parameter
			inputField = new MapRed.utils.HdfsFileField({
			    openDialog : param.input,
			    folderDialog : param.type == "hdfs-folder",
			    textfieldId : 'input-' + param.id,
			    fieldLabel : param.description,
			    myValue : param.value,
			    format : param.format,
			    allowBlank : !param.required,
			    required : param.required
			});

		    } else if (param.type == "label") {

			inputField = new Ext.Panel({

			    html : '<br>' + param.description,
			    border : false
			});

		    } else if (param.type == "list") {

			var values = new Array();

			for ( var a in param.values) {
			    values.push([ a, param.values[a] ]);
			}

			var store = new Ext.data.SimpleStore({
			    fields : [ 'key', 'value' ],
			    data : values
			});

			inputField = new Ext.form.ComboBox({
			    id : 'input-' + param.id,
			    hiddenName : 'input-' + param.id,
			    store : store,
			    displayField : 'value',
			    valueField : 'key',
			    typeAhead : false,
			    mode : 'local',
			    triggerAction : 'all',
			    emptyText : 'Select a value...',
			    fieldLabel : param.description,
			    selectOnFocus : true,
			    editable : false,
			    allowBlank : !param.required,
			    value : param.value
			});

		    } else if (param.type == "checkbox") {

			inputField = new Ext.form.Checkbox({
			    id : 'input-' + param.id,
			    hiddenName : 'input-' + param.id,
			    boxLabel : param.description,
			    hideLabel : true,
			    style : 'font-size:11px',
			    trueValue : param.values['true'],
			    falseValue : param.values['false']
			});

		    } else if (param.type == "number") {

			// normal parameter
			inputField = new Ext.form.NumberField({
			    id : 'input-' + param.id,
			    fieldLabel : param.description,
			    name : 'input-' + param.id,
			    value : param.value,
			    bodyStyle : 'padding-top:3px;',
			    allowBlank : !param.required
			});
		    } else {

			// normal parameter
			inputField = new Ext.form.TextField({
			    id : 'input-' + param.id,
			    fieldLabel : param.description,
			    name : 'input-' + param.id,
			    value : param.value,
			    bodyStyle : 'padding-top:3px;',
			    allowBlank : !param.required
			});
		    }

		    if (param.input) {

			// input parameter
			inputParams
				.insert(inputParams.items.length, inputField);
		    }

		}

		// update layout
		inputParams.doLayout();
		// outputParams.doLayout();

	    }
	});
