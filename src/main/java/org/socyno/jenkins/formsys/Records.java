package org.socyno.jenkins.formsys;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;

import javax.annotation.CheckForNull;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.model.AbstractModelObject;
import jenkins.model.ModelObjectWithContextMenu;

/**
 * Record collections.
 */
public class Records extends AbstractModelObject
			implements ModelObjectWithContextMenu  {
	public final int pageSize;
    public final int pageNumber;
    public final long totalSize;
    public final Form form;
    public final Pager pager;
    public final List<Map<String, String>> records;
    public final List<SearchFilter> filters;
    public final List<SearchOrder> orders;
    public final List<ExtendedField> displayFields;

    public Records (
            @CheckForNull Form form,
            List<SearchFilter> filter
        ) throws SysException {
    	this(form, filter, null, -1, -1);
    }
    
    public Records (
            @CheckForNull Form _form,
            List<SearchFilter> _filter,
            List<SearchOrder> _orders
        ) throws SysException {
    	this(_form, _filter, _orders, -1, -1);
    }

    public Records (
            @CheckForNull Form form,
            List<SearchFilter> filter,
            List<SearchOrder> orders,
            long _pageNumber,
            int _pageSize
        ) throws SysException {
    	this(form, filter, null, orders, -1, -1);
    }
        
    public Records (
          @CheckForNull Form _form,
          List<SearchFilter> _filters,
          List<ExtendedField> _fields,
          List<SearchOrder> _orders,
          int _pageNumber,
          int _pageSize
      ) throws SysException {
        form = _form;
        if ( _fields == null || _fields.size() == 0 ) {
        	_fields = form.displayFields;
        }
    	boolean idFound = false;
    	List<ExtendedField> _displayFields
    		= new ArrayList<ExtendedField>();
    	for( ExtendedField field : _fields ) {
    		if ( field != null ) {
    			PluginImpl.info("Display field found => " + field.getName());
    			if ( field.field.getForm().id == form.id ) {
    				_displayFields.add(field);
    			}
    			if ( !idFound ) {
    				idFound = field.field.isId();
    			}
    		}
    	}
    	if ( !idFound ) {
    		_displayFields.add( 0, new ExtendedField(
    			Fields.SYSFIELD.ID.get(form.getFields())
    		) );
    	}
    	displayFields = Collections.unmodifiableList(_displayFields);
        String select = "", counter = "";
        List<String> joins = new ArrayList<String>();
        for ( ExtendedField field : displayFields ) {
            if ( !select.isEmpty() ) {
                select += ", ";
            }
            String[] parsed = field.parseSQLJoins();
            select += parsed[0] + " AS "
            	   + PluginImpl.sqlQuoted2As(parsed[0]);
            for ( int k = 1; k < parsed.length; k++ ) {
            	if ( !joins.contains(parsed[k]) ) {
            		joins.add(parsed[k]);
            	}
            }
            if ( field.field.isId() || field.getType().rawIsArray() ) {
            	if ( !counter.isEmpty() ) {
            		counter += ", ";
                }
            	counter += PluginImpl.sqlCoalesce(parsed[0], "''");
            }
        }
        String orderby = "";
        String condition = "1 = 1";
        List<SearchFilter> filtersX
        	= new ArrayList<SearchFilter>();
        if ( _filters != null && _filters.size() > 0 ) {
            for( SearchFilter f : _filters ) {
                if ( f == null ) {
                	continue;
                }
                filtersX.add(f);
                if ( f.getField().getForm().id != form.id ) {
                	throw new SysException(
                		Messages.SCMFieldBelongToAnotherForm()
                	);
                }
                String[] js = f.field.parseSQLJoins();
                for ( int k = 1; k < js.length; k++ ) {
                	if ( !joins.contains(js[k]) ) {
                		joins.add(js[k]);
                	}
                }
                condition += String.format(
                	" AND (%1$s)",
                	f.parseCondition(js[0])
                );
            }
        }
        filters = Collections.unmodifiableList(filtersX);
        
        // fetch total size
        final List<Long> totalSizeX = new ArrayList<Long>(); 
        PluginImpl.getInstance().query( String.format(
        	"SELECT COUNT(DISTINCT %1$s) FROM %2$s AS %3$s"
        			+ " %4$s WHERE (%5$s)",
        	counter,
        	PluginImpl.sqlQuote2One("FORM__" + form.name),
        	PluginImpl.sqlQuote2One("me"),
        	Utils.stringJoin( " ",
        		joins.toArray(new Object[joins.size()])
        	),
        	condition
          ), new SQLTransaction() { @Override public void run(Connection conn, Object... o) throws SysException {
  		  	try {
  		  		ResultSet rs = (ResultSet)o[0];
  		  		rs.next(); totalSizeX.add(rs.getLong(1));
	        }
	        catch ( SQLException e ) {
	            throw new SysException(
	            	Messages.SCMFailedToInitializeField(),
	            	e
	            );
	        }
        } } );
        totalSize = totalSizeX.get(0);
        List<SearchOrder> ordersX
    		= new ArrayList<SearchOrder>();
        if ( _orders != null && _orders.size() > 0 ) {
            for ( SearchOrder order : _orders) {
                if ( order == null ) {
                    continue;
                }
                ordersX.add(order);
                String[] js = order.field.parseSQLJoins();
                for ( int k = 1; k < js.length; k++ ) {
                	if ( !joins.contains(js[k]) ) {
                  		joins.add(js[k]);
                  	}
                }
                orderby += String.format(
               	    "%1$s %2$s %3$s",
               	    (orderby.isEmpty() ? "" : ","),
               	    js[0],
               	    order.order.toString()
                );
            }
        }
        orders = Collections.unmodifiableList(ordersX);
        select = String.format(
        	"SELECT DISTINCT %1$s FROM %2$s AS %3$s %4$s WHERE(%5$s)",
        	select,
        	PluginImpl.sqlQuote2One("FORM__" + form.name),
        	PluginImpl.sqlQuote2One("me"),
        	Utils.stringJoin( " ",
        		joins.toArray(new Object[joins.size()])
        	),
        	condition
        );
        select += (orderby.isEmpty() ? "" : (" ORDER BY " + orderby));

        if ( _pageSize < 1 ) {
        	_pageSize = 1;
        }
        if ( _pageNumber < 1 ) {
        	_pageNumber = 1;
        }
        if ( (_pageNumber - 1) * _pageSize > totalSize ) {
        	_pageNumber = (int)(totalSize / _pageSize) + 1;
        }
        pageSize = _pageSize;
        pageNumber = _pageNumber;
        final List<Map<String, String>> _record
        	= new ArrayList<Map<String, String>>();
        PluginImpl.getInstance().SQLQueryWithPager(
    		select, pageNumber, pageSize, new SQLTransaction() { @Override public void run(Connection conn, Object... o) throws SysException {
  		  	try {
  		  		ResultSet rs = (ResultSet)o[0];
	            while ( rs.next() ) {
	                Map<String, String> d = new HashMap<String, String>();
	                for ( int i = 0; i < displayFields.size(); i++ ) {
	                    d.put(displayFields.get(i).getName(), rs.getString(i + 1));
	                }
	                _record.add(Collections.unmodifiableMap(d));
	            }
	            
	        }
	        catch ( SQLException e ) {
	            throw new SysException(
	            	Messages.SCMFailedToInitializeField(),
	            	e
	            );
	        }
        } } );
        records = Collections.unmodifiableList(_record);
        pager = new Pager(totalSize, pageSize, pageNumber);
    }

    public static Records find(Form form, long... ids) throws SysException {
    	List<SearchFilter> filter =
            new ArrayList<SearchFilter>();
        filter.add(new SearchFilter(
        	new ExtendedField(
        		Fields.SYSFIELD.ID.get(form.getFields())
        	),
        	Fields.OPERATORS.EQUALS,
        	Utils.longArrToStringArr(ids)
        ) );
        return new Records(form, filter, null, 1, ids.length);
    }
       
    public int size() {
        return records.size();
    }
    
    public Map<String, String> get(int index) {
        return records.get(index);
    }
    
    public SearchOrder[] getOrders() {
    	return orders.toArray(new SearchOrder[orders.size()]);
    }
    
    public SearchFilter[] getFilters() {
    	return filters.toArray(new SearchFilter[filters.size()]);
    }
    
    public Form getForm() {
        return form;
    }
        
    /**
     * Used when redirected to a record
     */
    public Record getDynamic(String token, StaplerRequest req, StaplerResponse resp)
    			throws SysException {
    	long id = 0;
    	try {
    		id = Utils.parseLong(token);
    	} catch ( NumberFormatException e ) {
    		throw new MissingRecordException(
    			form, token
    		);
    	}
    	Record record;
    	if ( id <= 0 ) {
    		record = Record.create(form);
    	} else {
    		record = Record.get(form, id);
    		Actions.Action action;
    		if ( (action = form.getActions().get(req.getParameter("action"), true)) != null ) {
    			record.setBindAction(action);
    		}
    	}
		record.getBindAction().checkAllowed(record);
		record.getBindAction().doPreHook(record);
    	return record;
    }
    
    @Override
	public String getDisplayName() {
		return form.display();
	}

    public String getUrl() {
        return Home.DEPLOY_MANAGER_URL + "/" + form.name + "/";
    }

	@Override
	public String getSearchUrl() {
		return getUrl();
	}

	@Override
	public ContextMenu doContextMenu(StaplerRequest arg0, StaplerResponse arg1) throws Exception {
		return null;
	}
}
