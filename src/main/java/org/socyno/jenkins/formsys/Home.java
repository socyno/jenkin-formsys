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

import hudson.Extension;
import hudson.model.RootAction;
import jenkins.model.Jenkins;
import jenkins.model.ModelObjectWithContextMenu;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import org.socyno.jenkins.formsys.Messages;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 * Home page of deploy manager.
 *
 * @author xiaojun.cai@99bill.com
 */
@Extension
public class Home implements RootAction, ModelObjectWithContextMenu {
    /** the URL name for deploy manager home page.*/
    public static final String DEPLOY_MANAGER_URL = "bill99scm";

    public String getIconFileName() {
        return "user.png";
    }

    public String getDisplayName() {
        return Messages.PluginDisplayName();
    }

    public String getUrlName() {
        return DEPLOY_MANAGER_URL;
    }
    
    public static String getUrl() {
    	return String.format(
    		"%s/%s",
    		Jenkins.getInstance().getRootUrl(),
    		DEPLOY_MANAGER_URL
    	);
    }
    
    private List<SearchFilter> parseRecordsFilters(
    		@CheckForNull
    		Form form,
    		String filtersJson
    ) throws SysException {
    	List<SearchFilter> filters
			= new ArrayList<SearchFilter>();
		if ( (filtersJson = Utils.trimOrEmpty(filtersJson)).isEmpty() ) {
			return filters;
		}
    	JSON jFilter = null;
		PluginImpl.info( String.format(
			Messages.SCMRecordsSearchFiltersInfo(),
			form.display(), filtersJson
		) );
    	try {
    		jFilter = JSONSerializer.toJSON(filtersJson);
    	} catch ( Exception e ) {
    		PluginImpl.warning(e);
    		throw new SysException(
    			Messages.SCMErrorInvalidSearchFilter()
    			+ " : Filter is an invalid JSON string."
    		);
		}
    	if ( jFilter == null || !jFilter.isArray() ) {
    		throw new SysException(
    			Messages.SCMErrorInvalidSearchFilter()
    			+ " : Parsed filter is not a JSON  array." 
    			
    		);
    	}
    	for( Object j : ((JSONArray)jFilter) ) {
    		if ( !(j instanceof JSONObject) ) {
	    		throw new SysException(
	    			Messages.SCMErrorInvalidSearchFilter()
	    			+ " : Parsed condition is not a JSON object."
	    		);
    		}
    		ExtendedField field = null;
    		Fields.OPERATORS operator = null;
    		Object o;
    		if ( (o = ((JSONObject)j).get("field")) != null ) {
    			field = form.getFields().parseExtended(o.toString());
    		}
    		if ( (o = ((JSONObject)j).get("operator")) != null ) {
    			try {
    				operator = Fields.OPERATORS.valueOf(o.toString());
    			} catch ( Exception ex ) { }
    		}
    		if ( field == null || operator == null ) {
	    		throw new SysException(
	    			Messages.SCMErrorInvalidSearchFilter()
	    			+ " : No or invalid filed or operator parsed => "
	    			+ ((JSONObject)j).toString()
	    		);
    		}
    		String[] values = null;
    		if ( (o = ((JSONObject)j).get("values")) != null ) {
    			if ( o instanceof JSONArray ) {
    				values = new String[((JSONArray)o).size()];
    				for (  int z = 0; z < values.length; z++) {
    					values[z] = ((JSONArray)o).getString(z);
    				}
    			}
    		}
    		if ( (values == null || values.length == 0) && operator.valueAllowed() ) {
    			throw new SysException(
	    			Messages.SCMErrorInvalidSearchFilter()
	    			+ " : No values parsed => "
	    			+ ((JSONObject)j).toString()
	    		);
    		}
    		filters.add(new SearchFilter(field, operator, values));
    	}
    	return filters;
    }
    
    private List<SearchOrder> parseRecordsOrders(
    		@CheckForNull
    		Form form,
    		String orderJson
    ) throws SysException {
    	List<SearchOrder> orders
			= new ArrayList<SearchOrder>();
		if ( (orderJson = Utils.trimOrEmpty(orderJson)).isEmpty() ) {
			return orders;
		}
    	JSON jOrders = null;
		PluginImpl.info( String.format(
			Messages.SCMRecordsSearchFiltersInfo(),
			form.display(), orderJson
		) );
    	try {
    		jOrders = JSONSerializer.toJSON(orderJson);
    	} catch ( Exception e ) {
    		PluginImpl.warning(e);
    		throw new SysException(
    			Messages.SCMErrorInvalidSearchOrder()
    			+ " : Orders is an invalid JSON string."
    		);
		}
    	if ( jOrders == null || !jOrders.isArray() ) {
    		throw new SysException(
    			Messages.SCMErrorInvalidSearchOrder()
    			+ " : Parsed orders is not a JSON array." 
    			
    		);
    	}
    	for( Object j : ((JSONArray)jOrders) ) {
    		if ( !(j instanceof JSONObject) ) {
	    		throw new SysException(
	    			Messages.SCMErrorInvalidSearchOrder()
	    			+ " : Parsed order item is not a JSON object."
	    		);
    		}
    		ExtendedField field = null;
    		SearchOrder.ORDER order = null;
    		Object o;
    		if ( (o = ((JSONObject)j).get("field")) != null ) {
    			field = form.getFields().parseExtended(o.toString(), true);
    		}
    		if ( (o = ((JSONObject)j).get("order")) != null ) {
    			try {
    				order = SearchOrder.ORDER.valueOf(o.toString());
    			} catch ( Exception ex ) { }
    		}
    		if ( field == null || order == null ) {
	    		throw new SysException(
	    			Messages.SCMErrorInvalidSearchOrder()
	    			+ " : No or invalid filed or order parsed => "
	    			+ ((JSONObject)j).toString()
	    		);
    		}
    		orders.add(new SearchOrder(field, order));
    	}
    	return orders;
    }
    
    private List<ExtendedField> parseRecordsFields(
    		@CheckForNull
    		Form form,
    		String fieldsJson
    ) throws SysException {
    	List<ExtendedField> fields
			= new ArrayList<ExtendedField>();
		if ( (fieldsJson = Utils.trimOrEmpty(fieldsJson)).isEmpty() ) {
			return fields;
		}
    	JSON jFields = null;
		PluginImpl.info( String.format(
			Messages.SCMRecordsSearchFiltersInfo(),
			form.display(), fieldsJson
		) );
    	try {
    		jFields = JSONSerializer.toJSON(fieldsJson);
    	} catch ( Exception e ) {
    		PluginImpl.warning(e);
    		throw new SysException(
    			Messages.SCMErrorInvalidSearchField()
    			+ " : Fields is an invalid JSON string."
    		);
		}
    	if ( jFields == null || !jFields.isArray() ) {
    		throw new SysException(
    			Messages.SCMErrorInvalidSearchField()
    			+ " : Parsed fields is not a JSON array." 
    			
    		);
    	}
    	for( Object j : ((JSONArray)jFields) ) {
    		if ( j == null ) {
    			continue;
    		}
    		ExtendedField field
    			= form.getFields().parseExtended(j.toString(), true);
    		if ( field == null ) {
    			continue;
    		}
    		fields.add(field);
    	}
    	return fields;
    }
    
    /**
     * Used when redirected to a form.
     * @param token the name of the team.
     * @param req the stapler request.
     * @param resp the stapler response.
     * @return the correct form records.
     * @throws SysException 
     */
    public Records getDynamic(String token, StaplerRequest req, StaplerResponse resp)
    			throws SysException {
    	Form form;
        return new Records(
    		form = Form.get(token),
    		parseRecordsFilters(form, req.getParameter("filter")),
    		parseRecordsFields(form, req.getParameter("display")),
    		parseRecordsOrders(form, req.getParameter("orders")),
    		Utils.parseInt(req.getParameter("page"), 1),
    		Utils.parseInt(req.getParameter("rows"), 50)
        );
    }

    public ContextMenu doContextMenu(StaplerRequest request, StaplerResponse response) throws Exception {
        ContextMenu menu = new ContextMenu();
        /***
        menu.add("dpmApply", getIconPath("images/24x24/new-package.png"), Messages.DeploymentApply());
        menu.add("dpmList", getIconPath("images/24x24/new-package.png"), Messages.DeploymentApplications());
        ***/
        return menu;
    }
}
