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

Ext.ns('MapRed.utils');

MapRed.utils.HdfsFileDialog = Ext.extend(Ext.Window, {

    outputTextfield : '',

    folderDialog : true,
    
    format: null,

    openDialog : true,

    nameTextField : null,

    newNameTextField : null,

    tree : null,

    form : null,

    initComponent : function() {

	// Folder
	this.nameTextField = new Ext.form.TextField({
	    fieldLabel : this.openDialog ? 'Folder' : 'Parent-Folder',
	    // disabled : false,
	    readOnly : true,
	    anchor : '100%',
	    allowBlank : false
	});

	// Name (only in save mode)
	this.newNameTextField = new Ext.form.TextField({
	    fieldLabel : 'Name',
	    disabled : false,
	    anchor : '100%',
	    allowBlank : this.folderDialog
	});

	// url
	var url = '';

	if (this.format != null && this.format != "") {

	    url = '../hdfs/format/' + this.format;
	    
	} else {

	    if (this.folderDialog) {
		url = '../hdfs/folders';
	    } else {
		url = '../hdfs/files';
	    }

	}

	// hidden textfield for open file dialog
	this.hiddenTextField = new Ext.form.TextField({
	    id : 'hidden-field-hdfs',
	    name : 'hidden-field-hdfs',
	    fieldLabel : 'Hidden',
	    hidden : true,
	    allowBlank : !(!this.folderDialog && this.openDialog)
	});

	// Filetree
	this.tree = new Ext.tree.TreePanel({
	    id : 'fileTree',
	    region : 'center',
	    useArrows : true,
	    autoScroll : true,
	    animate : false,
	    enableDD : false,
	    containerScroll : true,
	    allowBlank : true,

	    nameTextField : this.nameTextField,
	    newNameTextField : this.newNameTextField,
	    hiddenTextField : this.hiddenTextField,
	    folderDialog : this.folderDialog,
	    openDialog : this.openDialog,

	    loader : new Ext.tree.TreeLoader({
		dataUrl : url
	    }),

	    root : new Ext.tree.AsyncTreeNode({
		id : 'root',
		text : 'My Workspace',
		path : '',
		expanded : true
	    }),

	    autoScroll : true,

	    viewConfig : {
		forceFit : true
	    },

	    listeners : {
		click : {
		    fn : this.treeClickListener
		}
	    }

	});

	// Form-Panel
	this.form = new Ext.form.FormPanel({
	    frame : false,
	    border : false,
	    layout : 'border',
	    window : this,
	    monitorValid : true,
	    bodyStyle : 'padding: 5px; background: none',
	    items : [
		    this.tree,
		    new Ext.Panel({
			monitorValid : true,
			layout : 'form',
			border : false,
			frame : false,
			region : 'south',
			autoHeight : true,
			bodyStyle : 'padding-top: 8px;  background: none',
			items : this.openDialog ? [ this.hiddenTextField,
				this.nameTextField ] : [ this.hiddenTextField,
				this.nameTextField, this.newNameTextField ]
		    }) ],

	    buttons : [ {
		text : 'OK',
		id : 'ok-button',
		formBind : true,
		openDialog : this.openDialog,
		outputTextfield : this.outputTextfield,
		nameTextField : this.nameTextField,
		newNameTextField : this.newNameTextField,
		window : this,
		handler : this.onOkClick
	    }, {
		text : 'Cancel',
		window : this,
		handler : this.onCancelClick
	    } ]
	});

	var windowTitle = '';
	if (this.openDialog) {
	    if (this.folderDialog) {
		windowTitle = 'Input Folder Selection';
	    } else {
		windowTitle = 'Input File Selection';
	    }
	} else {
	    if (this.folderDialog) {
		windowTitle = 'Output Folder Selection';
	    } else {
		windowTitle = 'Output File Selection';
	    }
	}

	Ext.apply(this, {

	    width : 400,
	    height : 300,
	    title : windowTitle,
	    monitorValid : true,
	    frame : true,
	    layout : 'fit',
	    modal : true,

	    bodyStyle : 'padding: 8px',
	    items : [ this.form ]

	});

	// call parent
	MapRed.utils.HdfsFileDialog.superclass.initComponent.apply(this,
		arguments);

    },

    // ok button listener

    onOkClick : function() {

	var output = Ext.getCmp(this.outputTextfield);

	if (this.openDialog) {

	    output.setValue(this.nameTextField.getValue());

	} else {
	    if (this.nameTextField.getValue() == '/') {

		output.setValue('/' + this.newNameTextField.getValue());

	    } else {
		if (this.newNameTextField.getValue() != '') {

		    output.setValue(this.nameTextField.getValue() + '/'
			    + this.newNameTextField.getValue());

		} else {

		    output.setValue(this.nameTextField.getValue());

		}
	    }
	}

	this.window.close();
    },

    // cancel button listener

    onCancelClick : function() {
	this.window.close();
    },

    // update textfield on selection

    treeClickListener : function(node, event) {

	if (node == this.root) {

	    // root
	    this.nameTextField.setValue("/");

	} else {

	    this.nameTextField.setValue(node.attributes.path);

	}

	if (!this.folderDialog && this.openDialog) {
	    if (node.isLeaf()) {
		this.hiddenTextField.setValue(node.attributes.text);
	    } else {
		this.hiddenTextField.setValue('');
	    }
	} else {
	    this.hiddenTextField.setValue('');
	}

    }

});
