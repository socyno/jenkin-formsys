
package org.socyno.jenkins.formsys;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;

import javax.annotation.CheckForNull;

import org.socyno.jenkins.formsys.Messages;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A customized field.
 */
public class Groups {

    public static final Map<String, String> FIELDS = getFields();
    private final List<Group> groups = new ArrayList<Group>();
    private static final Group ALL_USERS = new Groups().new Group(
    	0, "ALL_USERS", Messages.SCMSysGroupAllUsersDisplay(), ""
    );
    
    private static Map<String, String> getFields() {
    	Map<String, String> m = new HashMap<String, String>();
    	m.put("ID", Messages.SCMGroupIdTitle());
    	m.put("NAME", Messages.SCMGroupNameTitle());
    	m.put("USERS", Messages.SCMGroupUsersTitle());
    	m.put("DISPLAY", Messages.SCMGroupDisplayTitle());
    	return Collections.unmodifiableMap(m);
    }
    
    private Groups() {
    	
    }
    
    public Groups (String... ids) throws SysException {
    	if ( ids == null || ids.length == 0 ) {
    		return;
    	}
    	long _id = 0;
    	List<Long> idList = new ArrayList<Long>();
    	List<String> nameList = new ArrayList<String>();
    	for ( String id : ids ) {
    		if ( !(id = Utils.trimOrEmpty(id)).isEmpty() ) {
    			if ( id.equals(ALL_USERS.getName()) ) {
    				groups.add(ALL_USERS);
    			} else if ( (_id = Utils.parseLong(id, 0)) > 0 ) {
    				idList.add(_id);
    			} else {
    				nameList.add(PluginImpl.sqlEscapeEquals(id));
    			}
    		}
    	}
    	String where = "";
    	if ( !idList.isEmpty() ) {
    		where += String.format(
    			"%1$s IN (%2$s)",
    			PluginImpl.sqlQuote2One("me", "ID"),
    			Utils.stringJoin(", ", idList.toArray())
    		);
    	}
    	if ( !nameList.isEmpty() ) {
    		where += (where.isEmpty() ? "" : " OR ") + String.format(
        			"%1$s$s IN ('%2$s')",
        			PluginImpl.sqlQuote2One("me", "NAME"),
        			Utils.stringJoin("', '", nameList.toArray())
        		);
    	}
    	if ( where.isEmpty() ) {
    		return;
    	}
    	String select = "";
    	for ( String field : new String[] {"ID", "NAME", "DISPLAY", "USERS"} ) {
    		select += (select.isEmpty() ? "" : ", ")
    			   + PluginImpl.sqlQuote2One("me", field);
    	}
    	PluginImpl.getInstance().query( String.format(
            "SELECT DISTINCT %1$s FROM %2$s AS %3$s WHERE( %4$s )",
            select,
            PluginImpl.sqlQuote2One("GROUPS"),
            PluginImpl.sqlQuote2One("me"),
            where
    	  ), new SQLTransaction() { @Override public void run(Connection conn, Object... o) throws SysException {
       		  	try {
       		  		ResultSet rs = (ResultSet)o[1];
       		  		while ( rs.next() ) {
				        groups.add( new Group (
				            rs.getLong(1),
				            rs.getString(2),
				            rs.getString(3),
				            rs.getString(4)
				        ) );
       		  		}
   		        }
   		        catch ( SQLException e ) {
   		            throw new SysException(
   		            	Messages.SCMFailedToInitializeField(),
   		            	e
   		            );
   		        }
        } } );
    }
    
    public Group containsUser(@CheckForNull Users.Item user) {
    	for ( int i = 0; i < groups.size(); i++ ) {
    		if ( groups.get(i).contains(user) ) {
    			return groups.get(i);
    		}
    	}
    	return null;
    }
        
    public static Groups find(String... ids) throws SysException {
    	return new Groups(ids);
    }
        
    public static Group get(String name) throws SysException {
    	Groups gs = new Groups(name);
    	return gs.size() > 0 ? gs.get(0) : null;
    }
    
    public static String getFieldDisplay(String field) {
    	return FIELDS.get(Utils.trimOrEmpty(field).toUpperCase());
    }
    
    public int size() {
        return groups.size();
    }
    
    public Group get(int index) {
        return groups.get(index);
    }
    
    public long[] getIds() {
    	long[] ids = new long[groups.size()];
    	for ( int i = 0; i < ids.length; i++ ) {
    		ids[i] = get(i).getId();
    	}
    	return ids;
    }
        
    public class Group {
        private long id;
        private String name;
        private String display;
        private final Users users;
        
        private Group(long _id, String _name, String _display, String _users) {
        	id = _id;
        	name = _name;
        	display = _display;
        	users = Users.find( Utils.splitThrowEmpty( 
        		",", _users, true
        	) );
        }
        
        public boolean contains(@CheckForNull Users.Item user) {
        	if ( getId() == ALL_USERS.getId() ) {
        		return true;
        	}
        	for ( int i = 0; i < users.size(); i++ ) {
        		if (users.get(i).getId().equals(user.getId())) {
        			return true;
        		}
        	}
        	return false;
        }
        
        public String getDisplay() {
            return display;
        }
        
        public long getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        public Users getUsers() {
            return users;
        }
    }
}
