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

MapRed.wizards.SubmitJob = Ext.extend(Ext.ux.Wiz, {
    initComponent : function() {

	Ext.apply(this, {
	    id : 'wizard',
	    title : 'Submit Job',
	    previousButtonText : '&lt; Back',
	    headerConfig : {
		title : 'Submit Job',
		image : '../images/submit-job.png',
		stepText : '{2}'
	    },
	    loadMaskConfig : {
		'default' : 'Please wait, validating input...'
	    },
	    cardPanelConfig : {
		defaults : {
		    // baseCls :
		    // 'x-small-editor',
		    bodyStyle : 'padding:13px;background-color:#ffffff;',
		    border : false
		}
	    },
	    width : 550,
	    height : 500,
	    cards : (this.tool == null ?[ new MapRed.wizards.SubmitJobTool(),
		    new MapRed.wizards.SubmitJobParameters() ] :[new MapRed.wizards.SubmitJobParameters({mytool: this.tool}) ]),

	    listeners : {
		finish : this.onFinish
	    }
	});

	MapRed.wizards.SubmitJob.superclass.initComponent
		.apply(this, arguments);

	if(this.tool == null){
		Ext.getCmp('toolsTree').getLoader().load(Ext.getCmp('toolsTree').root);
	}

    },

    onFinish : function() {

	this.showLoadMask(true, 'Submitting...');
	this.switchDialogState(false);

	var values = {};
	var formValues = {};

	// read all general params
	for ( var i = 0, len = this.cards.length; i < len; i++) {
	    formValues = this.cards[i].form.getValues(false);
	    for ( var a in formValues) {
		values[a] = formValues[a];
	    }
	}

	// read all input params
	var inputParams = Ext.getCmp('input-params');
	for ( var i = 0, len = inputParams.items.length; i < len; i++) {

	    var name = inputParams.items.get(i).getId();
	    var value = inputParams.items.get(i).getValue();
	    if (inputParams.items.get(i) instanceof Ext.form.Checkbox) {
		if (value == true) {
		    value = inputParams.items.get(i).trueValue;
		} else {
		    value = inputParams.items.get(i).falseValue;
		}
	    }
	    values[name] = value;
	}

	// set used tool
	if(this.tool == null){
		var tree = Ext.getCmp('toolsTree');
		var tool = tree.getSelectionModel().getSelectedNode().id;
		values['tool'] = tool;
	}else{
		values['tool'] = this.tool;
	}

	// submit job
	Ext.Ajax.request({
	    url : '../jobs/submit',
	    method : 'POST',
	    scope : this,
	    jsonData : values,

	    failure : function(response, request) {

		var obj = Ext.util.JSON.decode(response.responseText);
		
		// error
		Ext.Msg.show({
		    title : 'Exception',
		    msg : obj.message,
		    buttons : Ext.Msg.OK
		});

		// close wizard
		this.switchDialogState(true);
		this.close();
	    },

	    success : function(response, request) {

		var obj = Ext.util.JSON.decode(response.responseText);

		// successfully added
		Ext.Msg.show({
		    title : 'Submitting Job',
		    msg : obj.message,
		    buttons : Ext.Msg.OK
		});

		// close wizard
		this.switchDialogState(true);
		this.close();

		// reload tasks
		var jobTable = Ext.getCmp('jobtable');
		var storeJobs = jobTable.getStore();
		storeJobs.reload();

	    }
	});

    }

});
