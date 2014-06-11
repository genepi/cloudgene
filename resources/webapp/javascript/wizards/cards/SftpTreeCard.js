Ext.ns('MapRed.wizards');

MapRed.wizards.SftpTreeCard = Ext
	.extend(
		Ext.ux.Wiz.Card,
		{

		    serverField : null,

		    userField : null,

		    passwordField : null,

		    rootNode : null,

		    treeLoader : null,

		    error : false,

		    initComponent : function() {

			this.serverField = new Ext.form.TextField({
			    id : "server",
			    name : 'server',
			    value : '',
			    fieldLabel : 'Bucket Name',
			    allowBlank : false,
			    hidden : true
			});

				this.rootNode = new Ext.tree.AsyncTreeNode({
				    id : 'NOLOAD',
				    text : 'SFTP',
				    path : 'NOLOAD',
				    expanded : true
				}),

				Ext
					.apply(
						this,
						{
						    id : 'card2',
						    wizRef : this,
						    title : 'Import from SFTP',
						    monitorValid : true,
						    frame : false,
						    fileUpload : false,
						    border : false,
						    height : '100%',

						    onCardShow : this.reloadSftpTree,

						    defaults : {
							labelStyle : 'font-size:11px'
						    },
						    items : [
							    {
								border : false,
								bodyStyle : 'background:none;padding-bottom:30px;',
								html : 'Please choose file to upload to Cloudgene.'
							    },
							    {
								title : 'Sftp - Browser',
								id : 'fieldset-sftp-browser',
								xtype : 'fieldset',
								autoHeight : true,
								defaults : {
								    width : 210,
								    labelStyle : 'font-size:11px'
								},
								defaultType : 'textfield',
								items : [
									this.serverField,
									new Ext.tree.TreePanel(
										{
										    id : 'file-tree-sftp',
										    useArrows : true,
										    autoScroll : true,
										    animate : false,
										    enableDD : false,
										    containerScroll : true,
										    rootVisible: true,
										    allowBlank : true,

										    anchor : '100%',
										    autoHeight : false,
										    border : false,
										    height : 230,

										    loader : new Ext.tree.TreeLoader(
											    {
											    baseParams : {sftphost : null ,sftpuser: null ,sftppass: null ,sftpport: 22 }, 
												error : false,
												nodeParameter: 'path',
												dataUrl : '../sftp/files',
												handleFailure : function(
													response) {
												    Ext.Msg
													    .alert(
														    'Error',
														    'Problems connect Error: '+ response.responseText);
												    this.error = true;
												},
												listeners : {
												    beforeload : {
													fn : function(
														response) {

													    this.error = false;

													}
												    }
												}
											    }),

										    root : this.rootNode,

										    viewConfig : {
											forceFit : true
										    },

										    listeners : {
											click : {
											    fn : this.treeClickListener
											}
										    }

										}) ]
							    } ]
						});

			// call parent
			MapRed.wizards.SftpTreeCard.superclass.initComponent
				.apply(this, arguments);

		    },

		    reloadSftpTree : function() {

				Ext.getCmp('file-tree-sftp').getLoader().baseParams.sftphost = Ext.getCmp('sftp-server').getValue().replace("sftp://", "");
				Ext.getCmp('file-tree-sftp').getLoader().baseParams.sftpuser = Ext.getCmp('username').getValue();
				Ext.getCmp('file-tree-sftp').getLoader().baseParams.sftppass = Ext.getCmp('password').getValue();
				Ext.getCmp('file-tree-sftp').getLoader().baseParams.sftpport = Ext.getCmp('port').getValue();
				
			    sftpServer = Ext.getCmp('sftp-server').getValue().replace("sftp://", "");

			    this.rootNode = new Ext.tree.AsyncTreeNode({
				text : sftpServer,
                    		expanded : true,
				children : [
                                {  text: 'File system (/)', 
                                   id : '/',
                                   path : '/',
                                   expanded : false},
                                   { 
                                  text: 'Home folder (~/)',
                                  id : '~/',
                                  path : '~/',
                                  expanded : true
                                 }]
			    });

			    Ext.getCmp('file-tree-sftp').getLoader().dataUrl = '../sftp/files';
			    Ext.getCmp('file-tree-sftp').setRootNode(
				    this.rootNode);
			
			Ext.getCmp('server').setValue("");

			// start monitoring
			if (this.monitorValid) {
			    this.startMonitoring();
			}

		    },

		    treeClickListener : function(node, event) {

			if (!Ext.getCmp('file-tree-sftp').getLoader().error) {
			    Ext.getCmp("server").setValue(Ext.getCmp('sftp-server').getValue() + node.attributes.id);
			}

		    }
		});
