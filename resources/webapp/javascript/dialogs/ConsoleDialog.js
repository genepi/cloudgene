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

MapRed.dialogs.consoleWindow = Ext.extend(Ext.Window, {

    initComponent : function() {

	Ext.apply(this, {

	    layout : 'fit',
	    width : 800,
	    height : 600,
	    closable : true,
	    plain : false,
	    border : false,
	    title : 'Console Output',
	    items : [ new Ext.Panel({
		    border: false,
		    plain : false,
		    id: 'console-panel',
		    name: 'console-panel',
		    logFile : this.logFile,
		    bodyStyle: 'padding: 10px; background: #000000; color:#cccccc;',
		    autoScroll: true,
		    autoLoad : {
		        url : this.logFile
		        
		    }
		})]
	});
		
	MapRed.dialogs.consoleWindow.superclass.initComponent.call(this);

	}
    
});