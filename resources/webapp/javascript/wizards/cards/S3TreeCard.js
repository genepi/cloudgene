Ext.ns('MapRed.wizards');

MapRed.wizards.S3TreeCard = Ext
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
				    id : '1000genomes',
				    text : 'My S3 Buckets',
				    path : '',
				    expanded : true
				}),

				Ext
					.apply(
						this,
						{
						    id : 'card2',
						    wizRef : this,
						    title : 'Import from S3-Bucket.',
						    monitorValid : true,
						    frame : false,
						    fileUpload : false,
						    border : false,
						    height : '100%',

						    onCardShow : this.reloadS3Tree,

						    defaults : {
							labelStyle : 'font-size:11px'
						    },
						    items : [
							    {
								border : false,
								bodyStyle : 'background:none;padding-bottom:30px;',
								html : 'Please specify your Amazon S3 connection.'
							    },
							    {
								title : 'Amazon S3 - Browser',
								id : 'fieldset-amazon-browser',
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
										    id : 'file-tree-s3',
										    useArrows : true,
										    autoScroll : true,
										    animate : false,
										    enableDD : false,
										    containerScroll : true,
										    allowBlank : true,

										    anchor : '100%',
										    autoHeight : false,
										    border : false,
										    height : 230,

										    loader : new Ext.tree.TreeLoader(
											    {
												error : false,
												dataUrl : '../buckets/public',
												handleFailure : function(
													response) {
												    Ext.Msg
													    .alert(
														    'Error',
														    'The public S3-Bucket could not be found.');
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
			MapRed.wizards.S3TreeCard.superclass.initComponent
				.apply(this, arguments);

		    },

		    reloadS3Tree : function() {

			if (Ext.getCmp('fieldset-amazon-bucket-name')
				.getValue().inputValue == 'public') {

			    bucketName = Ext.getCmp('bucket-name-public')
				    .getValue();

			    this.rootNode = new Ext.tree.AsyncTreeNode({
				id : bucketName.replace("s3n://", ""),
				text : bucketName,
				path : '',
				expanded : true
			    });

			    Ext.getCmp('file-tree-s3').getLoader().dataUrl = '../buckets/public';
			    Ext.getCmp('file-tree-s3').setRootNode(
				    this.rootNode);
			} else {

			    bucketName = Ext.getCmp('bucket-name-private')
				    .getValue();

			    this.rootNode = new Ext.tree.AsyncTreeNode({
				id : bucketName,
				text : bucketName,
				path : '',
				expanded : true
			    });

			    Ext.getCmp('file-tree-s3').getLoader().dataUrl = '../buckets/private';
			    Ext.getCmp('file-tree-s3').setRootNode(
				    this.rootNode);

			}

			Ext.getCmp('server').setValue("");

			// start monitoring
			if (this.monitorValid) {
			    this.startMonitoring();
			}

		    },

		    treeClickListener : function(node, event) {

			if (!Ext.getCmp('file-tree-s3').getLoader().error) {
			    Ext.getCmp("server").setValue(
				    "s3n://" + node.attributes.id);
			}

		    }
		});
