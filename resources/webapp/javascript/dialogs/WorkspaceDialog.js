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

Ext.ns('MapRed.dialogs');

MapRed.view.WorkspacePanel = Ext.extend(Ext.Panel, {

    tree : null,

        filenameRenderer : function(value, p, record) {

		if(record.data.leaf){

			return record.data.text;
		
		}else{
		
			return '<a href="javascript:navigate(\''+record.data.path+'\');">'+record.data.text+'</a>';
		
		}
	
   		 },
   		 
   		 
       iconRenderer : function(value, p, record) {

		if(record.data.leaf){

			return '<img src="/images/icons/txt.png" />';
		
		}else{
		
			return '<img src="/images/icons/folder.png" />';
		
		}
	
   		 },

    initComponent : function() {

this.myReader = new Ext.data.JsonReader({
    fields : [ {
	name : 'text',
	type : 'string'
    }, {
    id: 'id',
	name : 'id',
	type : 'string'
    }, {
	name : 'path',
	type : 'string'
    },{
	name : 'size',
	type : 'string'
    },  {
	name : 'leaf',
	type : 'boolean'
    }]
});

this.myStore = new Ext.data.Store(
	{

	    autoDestroy : true,
	    baseParams: {node: 'root'},
	    url : '../hdfs/files',
	    reader : this.myReader,
	    autoLoad: true,
	    path : '',
	    current: ''
	    
	});

this.checkBoxSelMod = new Ext.grid.CheckboxSelectionModel({singleSelect:false});

// Filetree
	this.tree = new Ext.grid.GridPanel({
	id: 'file-grid',
	name: 'file-grid',
	selModel : this.checkBoxSelMod,
 columns : [ 
this.checkBoxSelMod,
 
 {
		id : 'icon-column',
		header : "",
		width : 20,
		dataIndex : 'leaf',
		renderer: this.iconRenderer
	    },
 
	{
		id : 'filename-column',
		header : "Name",
		width : 200,
		dataIndex : 'path',
		renderer: this.filenameRenderer
	    },{
		id : 'size-column',
		header : "Size",
		width : 100,
		dataIndex : 'size'
	    }],

	    store : this.myStore,

	    viewConfig : {
		forceFit : true
	    }
	     
	});



	Ext.apply(this, {

	    layout : 'fit',
	    width : 350,
	    title : 'My Workspace',
	    border: false,
tbar: 
       [{
          text: 'New Folder',
          icon : '../images/icons/add.png',
          handler: function(btn){


             Ext.Msg.prompt('Name', 'Please enter the new folder name:', function(btn, text){
	if (btn == 'ok') {
	
	          		var table = Ext.getCmp('file-grid');
		    	var store = table.getStore();
	
        Ext.Ajax.request({
			url : '../hdfs/new',
			params : {
			    parent: store.path,
		    	id : text
			},
			success : function(response) {
		    	store.reload();
		    }
		});

		}
          
          })



          }
       },
       {
          text: 'Delete',
                    icon : '../images/icons/delete.png',
          handler: function(btn){
          var table = Ext.getCmp('file-grid');
          		    	if (table.selModel.getCount() > 0){
          var array = new Array();          
          for (i = 0; i < table.selModel.getCount();i++){
          	array[i] = table.selModel.getSelections()[i].id;
          }
          
              Ext.Msg.confirm('Delete Files', 'Are you sure you want to delete the selected files?', function(btn, text) {
	if (btn == 'yes') {
        Ext.Ajax.request({
			url : '../hdfs/delete',
			params : {
		    	id : array
			},
			success : function(response) {
          		var table = Ext.getCmp('file-grid');
		    	var store = table.getStore();
		    	store.reload();
		    }
		});

		}
          
          })}}
       },{
          text: 'Rename',
          icon : '../images/icons/edit.png',
          handler: function(btn){
	          		var table = Ext.getCmp('file-grid');
		    	var store = table.getStore();
		    	if (table.selModel.getCount() > 0){
		    	if (table.selModel.getCount() > 1){
		    	
		    	  Ext.Msg.alert('Invalid Selection','Please select only one file to rename');
		    	  
		    	}else{
		    	
oldValue = table.selModel.getSelected().data.text;

             Ext.Msg.prompt('Name', 'Please enter the new file name:', function(btn, text){
	if (btn == 'ok') {

	
        Ext.Ajax.request({
			url : '../hdfs/rename',
			params : {
			    parent: store.path,
			    old: oldValue, 
		    	id : text
			},
			success : function(response) {
		    	store.reload();
		    }
		});

		}
          
          }, this, false, oldValue)
}
}

          }
       },{
		text : 'Import Data',
		tooltip : 'Import Data',
		xtype : 'tbbutton',
		icon : '../images/icons/import.png',

		menu : [ {
		    text : 'File Upload...',
		    handler : function(btn) {
		    var table = Ext.getCmp('file-grid');
		    var store = table.getStore();
			var wizard = new MapRed.wizards.ImportDataUpload({folder:store.path});
			wizard.show();

		    }
		}, {
		    text : 'From URL...',
		    handler : function(btn) {
		    var table = Ext.getCmp('file-grid');
		    var store = table.getStore();
			var wizard = new MapRed.wizards.ImportDataHttp({folder:store.path});
			wizard.show();

		    }
		}, {
		    text : 'From FTP Server...',
		    handler : function(btn) {
		    var table = Ext.getCmp('file-grid');
		    var store = table.getStore();
			var wizard = new MapRed.wizards.ImportDataFtp({folder:store.path});
			wizard.show();

		    }
		},{
		    text : 'From SFTP Server...',
		    handler : function(btn) {
		    var table = Ext.getCmp('file-grid');
		    var store = table.getStore();
			var wizard = new MapRed.wizards.ImportDataSftp({folder:store.path});
			wizard.show();

		    }
		}, {
		    text : 'From S3 Bucket...',
		    handler : function(btn) {
		    var table = Ext.getCmp('file-grid');
		    var store = table.getStore();
			var wizard = new MapRed.wizards.ImportDataS3({folder:store.path});
			wizard.show();

		    }
		}, {
		    text : 'From Local Filesystem...',
		    handler : function(btn) {
		    var table = Ext.getCmp('file-grid');
		    var store = table.getStore();
			var wizard = new MapRed.wizards.ImportLocalFile({folder:store.path});
			wizard.show();

		    }
		} ]

	    },{
          text: '',
          icon : '../images/icons/refresh.png',
          handler: function(btn){

	
	          	var table = Ext.getCmp('file-grid');
		    	var store = table.getStore();
		    	store.reload();
		    }
	          
          }



          
      
       ],
	    items : [ this.tree ]
	});
		
	MapRed.view.WorkspacePanel.superclass.initComponent.call(this);

	}
        
});

Ext.reg('workspacepanel',MapRed.view.WorkspacePanel);


navigate = function(path){
		    var table = Ext.getCmp('file-grid');
		    var store = table.getStore();
			store.current = store.path;
		    store.path = path;
store.load({ params: { node: path} });

}