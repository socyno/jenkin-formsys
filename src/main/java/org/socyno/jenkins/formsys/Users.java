
package org.socyno.jenkins.formsys;

import java.util.List;
import java.util.ArrayList;

import hudson.model.User;
import hudson.security.Permission;
import hudson.tasks.Mailer;
import hudson.tasks.Mailer.UserProperty;

import javax.annotation.CheckForNull;

/**
 * Users
 */
public class Users {

    private final List<Item> users
    	= new ArrayList<Item>();
    
    private Users (User... sysUsers) {
    	if ( sysUsers == null ) {
    		return;
    	}
    	for( User sysUser : sysUsers ) {
	    	if ( sysUser != null ) {
				users.add(new Item(sysUser));
	    	}
    	}
    }
    
    public static Users find(String... ids) {
    	List<User> sysUsers = new ArrayList<User>();
    	if ( ids != null ) {
        	for ( String id : ids ) {
        		sysUsers.add(User.get(id));
        	}
    	}
        return new Users(sysUsers.toArray(new User[sysUsers.size()]));
    }
    
    public static Item get(String id) {
    	User user = null;
    	if (id != null && (user = User.get(id)) != null ) {
    		return new Users(user).get(0);
    	}
    	return null;
    }
    
    public static Item getCurrent() {
    	User user = getCurrentSysUser();
    	if ( user != null ) {
    		return new Users(user).get(0);
    	}
    	return null;
    }
    
    public static User getCurrentSysUser() {
        return User.current();
    }

    public void add(@CheckForNull Item user) {
    	users.add(user);
    }
    
    public void clear() {
    	users.clear();
    }
    
    public Item get(int index) {
        return this.users.get(index);
    }
    
    public String[] getIds() {
    	String[] ids = new String[users.size()];
    	for ( int i = 0; i < ids.length; i++ ) {
    		ids[i] = get(i).getId();
    	}
    	return ids;
    }

    public void remove(int index) {
    	users.remove(index);
    }
    
    public int size() {
        return users.size();
    }
        
    public class Item {
    	private final User sysUser;
        
        private Item(@CheckForNull User _sysUser) {
        	sysUser = _sysUser;
        }
        
        public String getId() {
            return sysUser.getId();
        }
        
        public String getName() {
            return sysUser.getFullName();
        }
        
        public String getMail() {
			String email = null;
    		UserProperty property;
			if ( (property = sysUser.getProperty(Mailer.UserProperty.class)) != null ) {
				email = property.getAddress();
			}
            return email;
        }
        
        public String getDisplay() {
            return sysUser.getDisplayName();
        }
        
        public boolean hasPermission ( Permission permission ) {
        	return sysUser.hasPermission(permission);
        }
    }
}
