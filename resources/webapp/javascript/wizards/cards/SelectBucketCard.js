Ext.ns('MapRed.wizards');

MapRed.wizards.SelectBucketCard = Ext
	.extend(
		Ext.ux.Wiz.Card,
		{

		    serverField : null,

		    userField : null,

		    passwordField : null,

		    bucketNamePublicExamples : null,

			folder : "",

		    initComponent : function() {

			this.bucketNamePublicField = new Ext.form.TextField({
			    id : "bucket-name-public",
			    name : 'bucket-name-public',
			    value : 's3n://',
			    fieldLabel : 'S3 Bucket',
			    allowBlank : true
			});

			this.bucketNamePrivateField = new Ext.form.ComboBox({
			    id : "bucket-name-private",
			    name : 'bucket-name-private',
			    fieldLabel : 'S3 Bucket',
			    store : new Ext.data.Store({
				url : '../buckets/my',
				autoLoad : true,
				reader : new Ext.data.JsonReader({
				    id : 'bucket-reader',
				    fields : [ {
					name : 'text',
					type : 'string'
				    }, {
					name : 'id',
					type : 'string'
				    } ]
				})
			    }),
			    displayField : 'text',
			    allowBlank : true,
			    editable : false,
			    hidden : true,
			    typeAhead : true,
			    mode : 'local',
			    triggerAction : 'all',
			    emptyText : 'Choose a bucket',
			    selectOnFocus : true
			});

			this.bucketNamePrivateField
				.getStore()
				.on(
					{
					    'loadexception' : function() {

						var dialog = new MapRed.dialogs.AwsCredentialDialog(
							{
							    store : this,
							    privateRadioButton : Ext
								    .getCmp('private-readiobutton')
							});
						dialog.show();

					    }
					});

			Ext
				.apply(
					this,
					{
					    id : 'card1',
					    wizRef : this,
					    title : 'Import from S3-Bucket.',
					    monitorValid : true,
					    frame : false,
					    fileUpload : true,
					    border : false,
					    height : '100%',
					    defaults : {
						labelStyle : 'font-size:11px'
					    },
					    folder: this.folder,
					    items : [
						    {
							border : false,
							bodyStyle : 'background:none;padding-bottom:30px;',
							html : 'Please specify your Amazon S3 connection.'
						    },
						    {
							title : '',
							id : 'fieldset-target',
							xtype : 'fieldset',
							autoHeight : true,
							defaults : {
							    width : 210,
							    labelStyle : 'font-size:11px'
							},
							defaultType : 'textfield',
							items : [ new Ext.form.TextField(
								{
								    id : 'path',
								    fieldLabel : 'Folder Name',
								    allowBlank : false,
								    value: this.folder
								}) ]
						    },

						    {
							title : 'Amazon S3 Bucket',
							id : 'fieldset-amazon-bucket-group',
							xtype : 'fieldset',
							autoHeight : true,
							defaults : {
							    width : 210,
							    labelStyle : 'font-size:11px'
							},
							defaultType : 'textfield',
							items : [
								{
								    fieldLabel : 'Type',
								    title : 'Amazon S3',
								    id : 'fieldset-amazon-bucket-name',
								    xtype : 'radiogroup',
								    defaults : {
									xtype : "radio",
									name : "type",
									width : 210,
									labelStyle : 'font-size:11px'
								    },

								    autoHeight : true,
								    items : [
									    {
										boxLabel : "Public",
										inputValue : "public",
										bucketNamePublicField : this.bucketNamePublicField,
										bucketNamePrivateField : this.bucketNamePrivateField,
										listeners : {
										    'check' : function(
											    checkbox,
											    checked) {
											if (checked) {
											    this.bucketNamePublicField
												    .setVisible(true);
											    this.bucketNamePrivateField
												    .setVisible(false);
											}
										    }
										},
										checked : true
									    },
									    {
										boxLabel : "Private",
										inputValue : "private",
										id : 'private-readiobutton',
										bucketNamePublicField : this.bucketNamePublicField,
										bucketNamePrivateField : this.bucketNamePrivateField,
										listeners : {
										    'check' : function(
											    checkbox,
											    checked) {
											if (checked) {
											    this.bucketNamePublicField
												    .setVisible(false);
											    this.bucketNamePrivateField
												    .setVisible(true);
											}
										    }
										}
									    } ]
								},
								this.bucketNamePublicField,
								this.bucketNamePrivateField,
								{
								    xtype : 'box',
								    html : '<br/><br/><b>Examples for Public S3 Buckets:</b><ul><li>s3n://elasticmapreduce/samples/cloudburst</li><li>s3n://1000genomes</li><li>s3n://1000genomes/data/HG00096</li><li>s3n://cloudgene.data</li></ul>'

								} ]
						    } ]
					});

			// call parent
			MapRed.wizards.SelectBucketCard.superclass.initComponent
				.apply(this, arguments);

		    }

		});
