package org.socyno.jenkins.formsys;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.CheckForNull;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.model.AbstractModelObject;
import jenkins.model.ModelObjectWithContextMenu;

public class Form extends AbstractModelObject
			implements ModelObjectWithContextMenu  {
	public final long id;
    public final String name;
    public final boolean withStatus;
    public String display = null;
    public String description = null;
    private Fields fields = null;
    private Actions actions = null;
    public final List<ExtendedField> displayFields
				= new ArrayList<ExtendedField>();
    private Map<String, List<String>> uniques = null;
  
    private Form (
    	long id,
    	@CheckForNull String name,
    	boolean withStatus,
    	String[] columns
    ) throws SysException {
    	this.id = id;
    	this.withStatus = withStatus;
    	this.name = name.trim().toUpperCase();
    	if ( columns != null && columns.length > 0 ) {
    		for ( String column : columns ) {
    			if ( (column = Utils.trimOrEmpty(column)).isEmpty() ) {
    				continue;
    			}
    			ExtendedField field;
    			if ( (field = this.fields.parseExtended(name.toUpperCase(), true)) == null ) {
    				continue;
    			}
    			this.displayFields.add(field);
    		}
    	}
    }

    public static Form get(long id) throws SysException {
    	return get(((Long)id).toString(), false);
    }
    
    public static Form get(long id, boolean missingAsNull) throws SysException {
    	return get(((Long)id).toString(), missingAsNull);
    }
    
    public static Form get(String name) throws SysException {
    	return get(name, false);
    }
    
    public static Form get(String name, boolean missingAsNull) throws SysException {
    	Form[] forms;
    	if ( (forms = get(new String[]{name})).length == 0 ) {
    		if ( missingAsNull ) {
    			return null;
    		}
    		throw new SysException( String.format(
    			Messages.SCMErrorMissingForm(),
    			name
    		) );
    	}
    	return forms[0];
    }
    
    public static Form[] get(String... names) throws SysException {
    	String select = String.format(
    		"SELECT DISTINCT %1$sd, %2$sd, %3$sd, %4$sd"
    			+ ", %5$sd, %6$sd FROM %7$s",
    		PluginImpl.sqlQuote2One("ID"),
    		PluginImpl.sqlQuote2One("NAME"),
    		PluginImpl.sqlQuote2One("DISPLAY"),
    		PluginImpl.sqlQuote2One("WITHSTATUS"),
    		PluginImpl.sqlQuote2One("DESCRIPTION"),
    		PluginImpl.sqlQuote2One("DISPLAYFIELDS"),
    		PluginImpl.sqlQuote2One("FORMS")
    	);
    	if ( names != null && names.length > 0 ) {
        	String where = "";
    		for ( String name : names ) {
    			if ( (name = Utils.trimOrEmpty(name)).isEmpty() ) {
    				continue;
    			}
    			Long ifid;
    			if ( (ifid = Utils.parseLongObj(name, null)) == null ) {
    				where += (where.isEmpty() ? "" : " OR " ) + String.format(
    					" %1$s = '%2$s'",
    					PluginImpl.sqlQuote2One("NAME"),
    					PluginImpl.sqlEscapeEquals(name)
    				);
    			} else {
    				where += (where.isEmpty() ? "" : " OR " ) + String.format(
    					" %1$s = '%2$d'",
    					PluginImpl.sqlQuote2One("ID"),
    					ifid.longValue()
    				);
    			}
    		}
    		select += " WHERE " + (where.isEmpty() ? "1 = 0" : where);
    	}
    	final List<Form> forms = new ArrayList<Form>();
    	PluginImpl.getInstance().query( select, new SQLTransaction() {
    		@Override
    		public void run(Connection conn, Object... o) throws SysException {
       		  	try {
       		  		ResultSet rs = (ResultSet)o[0];
	       		  	while ( rs.next() ) {
	       		  		Form form = new Form(
	       		  			rs.getLong(1),
	       		  			rs.getString(2),
	       		  			rs.getBoolean(4),
	       		  			Utils.splitThrowEmpty(",", rs.getString(6), true)
	       		  		);
	       		  		form.display = rs.getString(3);
	       	            form.description = rs.getString(5);
	       	            forms.add(form);
	       		  	}
   		        }
   		        catch ( SQLException e ) {
   		            throw new SysException(
   		            	Messages.SCMFailedToInitializeForm(),
   		            	e
   		            );
   		        }
        } } );
    	return forms.toArray(new Form[forms.size()]);
    }
    
    public Actions getActions() throws SysException {
    	if ( actions == null ) {
    		actions = new Actions(this);
    	}
    	return actions;
    }
    
    public Fields getFields() throws SysException {
    	if ( fields == null ) {
        	fields = new Fields(this);
    	}
        return fields;
    }
    
    public Map<String, List<String>> getUniques() throws SysException {
    	if ( uniques == null ) {
    		uniques = new HashMap<String, List<String>>();
    	    PluginImpl.getInstance().query( String.format(
    	    	"SELECT %1$s, %2$s FROM %3$s AS %4$s"
    	    			+ " LEFT JOIN %5$s AS %6s ON %7$s = %8$s"
    	    			+ " WHERE ( %9$s = '%10$s' )"
    	    			+ " ORDER BY %11$s",
    	    	(Object[])PluginImpl.sqlQuote (
    	    		new String[] {"u", "NAME"},
    	    		new String[] {"f", "NAME"},
    	    		new String[] {"UNIQUES"},
    	    		new String[] {"u"},
    	    		new String[] {"FIELDS"},
    	    		new String[] {"f"},
    	    		new String[] {"f", "ID"},
    	    		new String[] {"u", "FIELD"},
    	    		new String[] {"u", "FORM"},
    	    		new String[] {this.name},
    	    		new String[] {"u", "ORDER"}
    	      ) ), new SQLTransaction() { @Override public void run(Connection conn, Object... o) throws SysException {
    	   		  	try {
    	   		  		ResultSet rs = (ResultSet)o[0];
    	                while ( rs.next() ) {
    	                	String field = rs.getString(2);
    	                	String name = rs.getString(1).toLowerCase();
    	                	if ( !uniques.containsKey(name) ) {
    	                		uniques.put(name, new ArrayList<String>());
    	                	}
    	                	if ( !uniques.get(name).contains(field) ) {
    	                		uniques.get(name).add(field);
    	                	}
    	                }
    		        }
    		        catch ( SQLException e ) {
    		            throw new SysException(
    		            	Messages.SCMFailedToInitializeForm(),
    		            	e
    		            );
    		        }
    	    } } );
    	}
    	return uniques;
    }

	public String display() {
		return display == null ? name : display;
	}
    
	public String getDisplayName() {
		return display();
	}

	public ContextMenu doContextMenu(StaplerRequest arg0, StaplerResponse arg1) throws Exception {
		return null;
	}

	public String getSearchUrl() {
		return name;
	}
    
    public String getUrl() {
        return String.format("%1$s/%2$s", Home.getUrl(),  name);
    }
    
    public String getUrlName() {
        return name;
    }
}
