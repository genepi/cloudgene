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

MapRed.utils.HdfsFileField = Ext.extend(Ext.form.CompositeField, {

    openDialog : true,

    folderDialog : false,

    textfieldId : '',

    myValue : '',

    field : null,

    button : null,

    required : true,

    format : null,
    
    initComponent : function() {

	items:

	// input field
	this.field = new Ext.form.TextField({
	    id : '-' + this.textfieldId,
	    name : this.textfieldId,
	    value : this.myValue,
	    readOnly : true,
	    width : 250,
	    allowBlank : !this.required
	});

	// browse button
	this.button = new Ext.Button({
	    text : 'Browse...',
	    width : 70,
	    myTextfieldId : '-' + this.textfieldId,
	    myOpenDialog : this.openDialog,
	    myFolderDialog : this.folderDialog,
	    format : this.format,
	    handler : function() {

		if (this.myFolderDialog) {

		    // Folder
		    var dialog = new MapRed.utils.HdfsFileDialog({
			folderDialog : true,
			openDialog : this.myOpenDialog,
			outputTextfield : this.myTextfieldId,
			format : this.format
		    });
		    dialog.show();

		} else {

		    // File
		    var dialog = new MapRed.utils.HdfsFileDialog({
			folderDialog : false,
			openDialog : this.myOpenDialog,
			outputTextfield : this.myTextfieldId,
			format : this.format
		    });
		    dialog.show();

		}

	    },
	    cls : 'x-form-file-btn'
	// bodyStyle: 'margin-left:5px;'
	});

	// panel configuration
	Ext.apply(this, {
	    id : this.textfieldId + '-panel',
	    layout : 'column',
	    border : false,
	    anchor : '100%',
	    autoHeight : true,
	    items : [ this.field, {
		width : '5px',
		border : false,
		html : '&nbsp;'
	    }, this.button ]
	});

	// call parent
	MapRed.utils.HdfsFileField.superclass.initComponent.apply(this,
		arguments);
    },

    // returns value
    getValue : function() {
	return this.field.value;
    },

    // returns inputfield id
    getId : function() {
	return this.textfieldId;
    }

});

Ext.reg('hdfsfilefield', MapRed.utils.HdfsFileField);