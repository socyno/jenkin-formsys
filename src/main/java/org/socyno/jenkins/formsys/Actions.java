/*
 * The MIT License
 *
 * Copyright 2013 Sony Mobile Communications AB. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.socyno.jenkins.formsys;


import java.sql.*;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;

import org.socyno.jenkins.formsys.Messages;

/**
 * A customized field.
 */
public class Actions  {

    private final Form form;
    private final List<Action> actions
    	= new ArrayList<Action>();
    public static final String ACTION_NEW = "new";
    public static final String ACTION_VIEW = "view";
    public static final String ACTION_EDIT = "edit";
    private static final Logger logger = Logger.getAnonymousLogger();
    
    public Actions (@CheckForNull Form form) throws SysException {
        this.form = form;
        PluginImpl.getInstance().query(
              "SELECT `ID`, `NAME`, `DISPLAY`, `DESCRIPTION`, `CONSCRIPT`,"
              + " `VRFSCRIPT`, `POSSCRIPT`, `PRESCRIPT`, `PERGROUPS`" 
              + " FROM `ACTIONS` WHERE(`FORM`='" + form.name + "')",
            new SQLTransaction() { @Override public void run(Connection conn, Object... o) throws SysException {
        		  try {
            		  ResultSet rs = (ResultSet)o[0];
            		  while ( rs.next() ) {
                      	actions.add( new Action (
                              rs.getLong(1),
                              Utils.trimOrEmpty(rs.getString(2)),
                              rs.getString(3),
                              rs.getString(4),
                              rs.getString(5),
                              rs.getString(6),
                              rs.getString(7),
                              rs.getString(8),
                              rs.getString(10)
                          ) );
                      }
        		  } catch ( SQLException e ) {
                      throw new SysException(
                    		  Messages.SCMFailedToInitializeActions(), e
                      );
                  }
        } } );
        if ( get(ACTION_NEW, true) == null ) {
        	actions.add( new Action(
        		0, ACTION_NEW, Messages.SCMActionDefaultNewDisplay(),
        		null, null, null, null, null, null
        	) );
        }
        if ( get(ACTION_VIEW, true) == null ) {
        	actions.add( new Action(
        		0, ACTION_VIEW, Messages.SCMActionDefaultViewDisplay(),
        		null, null, null, null, null, null
        	) );
        }
        if ( get(ACTION_EDIT, true) == null ) {
        	actions.add( new Action(
        		0, ACTION_EDIT, Messages.SCMActionDefaultEditDisplay(),
        		null, null, null, null, null, null
        	) );
        }
    }
    
    public Action get(String name,  boolean missingAsNull)  throws MissingActionException {
    	name = Utils.trimOrEmpty(name).toLowerCase();
    	for ( Action a : actions ) {
    		logger.fine("Found action : name = " + a.getName());
    		if ( a.getName().toLowerCase().equals(name) ) {
    			return a;
    		}
    	}
    	if ( missingAsNull ) {
    		return null;
    	}
    	throw new MissingActionException(form, name);
    }
    
    public Action get(String name)
     	   throws MissingActionException {
    	return get(name, false);
    }

    public Action[] toArray() {
        return actions.toArray(new Action[actions.size()]);
    }
    
    public  class Action {
        private long id;
        private String name;
        private String display;
        private String conscript;
        private String vrfscript;
        private String posscript;
        private String prescript;
        private final Groups pergroups;
        private String description;
        
        private Action(long id, String name, String display, String description,
              String conscript, String vrfscript, String posscript,
              String prescript, String pergroups ) throws SysException {
          this.id = id;
          this.name = name;
          this.display = display;
          this.description = description;
          this.conscript = conscript;
          this.vrfscript = vrfscript;
          this.posscript = posscript;
          this.prescript = prescript;
          this.pergroups = new Groups( Utils.splitThrowEmpty(
        	 ",", pergroups, true
          ) );
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getDisplay() {
        	return Utils.nullOrBlank(display, name);
        }
        
        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }
        

        public boolean isAllowed(Record record)
        	   throws SysException {
        	return isAllowed(record, false);
        }
        
        public boolean isAllowed(Record record, boolean forSave)
        	   throws SysException {
        	Users.Item currUser = Users.getCurrent();
        	if ( currUser == null ) {
        		PluginImpl.warning( String.format(
        			"Action(%1$s) permission check :"
        		  + "No current user found, you need to login.",
        		    getDisplay()
        		) );
        		return false;
        	}
        	Groups.Group included = null;
        	if ( PluginImpl.isSystemAdministrator()
             || (included = pergroups.containsUser(currUser) ) != null) {
        		PluginImpl.warning( String.format(
        			"Action(%1$s) permission check : Current user is %2$s.",
        			getDisplay(),
        			included == null ? "administrator" : (
        				"in group " + included.getDisplay()
        			)
        		) );
        	} else {
        		return false;
        	}
        	
        	if ( (conscript = Utils.trimOrEmpty(conscript)).isEmpty() ) {
        		return true;
        	}
          	while ( conscript.startsWith(":") ) {
          		Actions.Action a
          			= form.getActions().get(conscript.substring(1), true);
          		if ( a == null ) {
          			break;
          		}
          		conscript = a.conscript;
          	}
        	Map<String, Object> vars = new HashMap<String, Object>();
        	vars.put("record", record);
        	vars.put("forSave", forSave);
        	Object val;
        	try {
        		val = Utils.evalGroovy(conscript, vars);
        	} catch ( RuntimeException e ) {
        		throw new HookException(
        			HookException.HOOK.CONDITION,
        			form,
        			HookException.TYPE.RUNTIME,
        			e
        		);
        	}
        	PluginImpl.warning( String.format(
    			"Action(%1$s) permission check : hook return %2$s.",
    			getDisplay(),
    			Utils.nullAsEmpty(val).toString()
    		) );
        	if ( !(val instanceof Boolean) ) {
        		throw new HookException(
        			HookException.HOOK.CONDITION,
        			form,
        			HookException.TYPE.OUTPUT
        		);
        	}
        	return (Boolean)val;
        }
        
        
		public void checkAllowed(Record record, boolean forSave) throws SysException {
			if ( !isAllowed(record, forSave) ) {
				throw new SysException( String.format(
			        Messages.SCMErrorActionPermissionDenied(),
			        getDisplay()
			    ) );
			}
		}
		public void checkAllowed(Record record) throws SysException {
			checkAllowed(record, false);
		}
		
        public void doPostHook(Record record) throws HookException, SysException {
          	if ( (posscript = Utils.trimOrEmpty(posscript)).isEmpty() ) {
          		return;
          	}
          	while ( posscript.startsWith(":") ) {
          		Actions.Action a
          			= form.getActions().get(posscript.substring(1), true);
          		if ( a == null ) {
          			break;
          		}
          		posscript = a.posscript;
          	}
          	Map<String, Object> vars = new HashMap<String, Object>();
          	vars.put("record", record);
          	try {
          		Utils.evalGroovy(posscript, vars);
          	} catch ( RuntimeException e ) {
          		throw new HookException(
          			HookException.HOOK.POSTSAVE,
          			form,
          			HookException.TYPE.RUNTIME,
          			e
          		);
          	}
         }
        
		public void doPreHook(Record record) throws HookException, SysException {
			doPreHook(record, false);
		}
        
        public void doPreHook(Record record, boolean forSave) throws HookException, SysException {
        	if ( (prescript = Utils.trimOrEmpty(prescript)).isEmpty() ) {
        		return;
        	}
          	while ( prescript.startsWith(":") ) {
          		Actions.Action a
          			= form.getActions().get(prescript.substring(1), true);
          		if ( a == null ) {
          			break;
          		}
          		prescript = a.prescript;
          	}
        	Map<String, Object> vars = new HashMap<String, Object>();
        	vars.put("record", record);
        	vars.put("forSave", forSave);
        	try {
        		Utils.evalGroovy(prescript, vars);
        	} catch ( RuntimeException e ) {
        		throw new HookException(
        			HookException.HOOK.PREPARE,
        			form,
        			HookException.TYPE.RUNTIME,
        			e
        		);
        	}
        }
        
        public void doVerifyHook(Record record) throws HookException, SysException {
          	if ( (vrfscript = Utils.trimOrEmpty(vrfscript)).isEmpty() ) {
          		return;
          	}
          	while ( vrfscript.startsWith(":") ) {
          		Actions.Action a
          			= form.getActions().get(vrfscript.substring(1), true);
          		if ( a == null ) {
          			break;
          		}
          		vrfscript = a.vrfscript;
          	}
          	Map<String, Object> vars = new HashMap<String, Object>();
          	vars.put("record", record);
          	try {
          		Utils.evalGroovy(vrfscript, vars);
          	} catch ( RuntimeException e ) {
          		throw new HookException(
          			HookException.HOOK.VERIFY,
          			form,
          			HookException.TYPE.RUNTIME,
          			e
          		);
          	}
         }
    }
}
