package org.socyno.jenkins.formsys;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import javax.annotation.CheckForNull;
import javax.servlet.ServletException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.fileupload.FileItem;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.socyno.jenkins.formsys.Fields.TYPE;

import org.socyno.jenkins.formsys.Messages;

import hudson.model.AbstractModelObject;
import hudson.model.Hudson;
import hudson.model.Descriptor.FormException;
import jenkins.model.ModelObjectWithContextMenu;

public class Record extends AbstractModelObject
			implements ModelObjectWithContextMenu  {
    private long id;
    public final Form form;
    private static final String DKEY_VALUE = "value";
    private static final String DKEY_ORIGIN = "origin";
    private final Map<String, Map<String, Object>> data
    		= new HashMap<String, Map<String, Object>>();
    private Actions.Action bindAction = null;

    public static Record create(Form form)
    		throws SysException {
        return new Record(form);
    }
    
    public static Record get(@CheckForNull Form form, long id)
    		throws SysException {
    	Record[] result = get(form, new long[]{id});
    	if ( result.length < 1 ) {
    		throw new MissingRecordException(form, id);
    	}
        return result[0];
    }
    
    public static Record[] get(@CheckForNull Form form, long[] ids)
    		throws SysException {
    	if ( ids == null || ids.length == 0 ) {
    		return new Record[0];
    	}
    	String idName = Fields.SYSFIELD.ID.toString();
    	List<ExtendedField> fields = form.getFields().getExtended(false);
    	Records records = new Records (
			form,
			Arrays.asList( new SearchFilter[] {
				new SearchFilter(
						new ExtendedField(form.getFields().get(idName)),
						Fields.OPERATORS.EQUALS,
						Utils.longArrToStringArr(ids)
					)
		    	} ),
			form.getFields().getExtended(false),
			Arrays.asList( new SearchOrder[] {
				new SearchOrder(
						new ExtendedField(form.getFields().get(idName)),
						SearchOrder.ORDER.ASC
					)
		    	} ), 1, Integer.MAX_VALUE
    	);
    	
    	long currId = 0, nextId = 0;
    	Map<String, String> record = null;
    	Map<String, List<String>> data = new HashMap<String, List<String>>(); 
    	List<Record> result = new ArrayList<Record>();
    	for ( int i = 0; i < records.size(); i++ ) {
    		record = records.get(i);
    		if ( !record.containsKey(Fields.SYSFIELD.ID.toString()) ) {
    			continue;
    		}
    		String idStr = record.get(Fields.SYSFIELD.ID.toString());
    		if ( idStr == null || (nextId = Utils.parseLong(idStr, 0)) == 0 ) {
    			continue;
    		}
    		if ( currId == 0 ) {
    			currId = nextId;
    		}
			if ( nextId != currId ) {
				result.add(new Record(form, currId, data));
				data.clear(); currId = nextId;
			}
    		for ( ExtendedField f : fields ) {
    			String name = f.getName();
    			String value = record.get(name);
    			if ( !f.getType().rawIsArray() && data.containsKey(name) ) {
    				continue;
    			}
    			if ( !data.containsKey(name) ) {
    				data.put(name, new ArrayList<String>());
    			}
    			data.get(name).add(value);
    		}
    	}
		result.add(new Record(form, nextId, data));
    	return result.toArray(new Record[result.size()]);
    }
    
    public Form getForm() {
        return form;
    }

    private Record (@CheckForNull Form f)
    		throws SysException {
    	id = 0;
    	form = f;
    }
    
    private Record (
    	@CheckForNull Form _form,
    	long _id,
    	@CheckForNull Map<String, List<String>> _data
    ) throws SysException {
    	id = _id;
    	form = _form;
    	for ( ExtendedField f : form.getFields().getExtended(false) ) {
            String fn = f.getName();
            TYPE ft = f.field.type;
            List<String> origin = _data.get(fn);
            data.put(fn, new HashMap<String, Object>());
            data.get(fn).put(DKEY_ORIGIN, ft.rawFromArray(
            		origin.toArray(new String[origin.size()])
            ) );
        }
    }
        
    public Actions.Action getBindAction() throws SysException {
    	if ( isNew() ) {
    		if (bindAction == null || bindAction.getName() != Actions.ACTION_NEW) {
    			bindAction = form.getActions().get(Actions.ACTION_NEW);
    		}
    	} else if ( bindAction == null ) {
    		bindAction = form.getActions().get(Actions.ACTION_VIEW);
    	}
    	return bindAction;
    }
        
    public void setBindAction(Actions.Action action) {
    	bindAction = action;
	}
        
    public long getId() {
        return id;
    }
        
    public boolean isNew() {
        return getId() <= 0;
    }
        
    public Fields getFields() throws SysException {
        return form.getFields();
    }

	public Fields.Field getField (String name) throws SysException {
	    return form.getFields().get(name);
	}
        
    public Object getFieldValue(@CheckForNull Fields.Field field, boolean origin)
    		throws SysException {
    	if ( field.getForm().id != getForm().id ) {
            throw new SysException(Messages.SCMFieldBelongToAnotherForm());
        }
        if ( field.isId() ) {
        	return getId();
        }
        Map<String, Object> fieldData;
        if ( (fieldData = data.get(field.name)) == null ) {
        	return null;
        }
        if ( !origin && fieldData.containsKey(DKEY_VALUE) ) {
        	return fieldData.get(DKEY_VALUE);
        }
        if ( this.isNew() ) {
        	return field.getDefault();
        }
        if ( fieldData.containsKey(DKEY_ORIGIN) ) {
        	if ( !fieldData.containsKey("loaded") ) {
            	fieldData.put(DKEY_ORIGIN, field.type.rawToObject(
            		field, fieldData.get(DKEY_ORIGIN)
            	) );
            }
            fieldData.put("loaded", true);
            return fieldData.get(DKEY_ORIGIN);
        }
        return null;
    }
    
    public Object getFieldOrigin(@CheckForNull Fields.Field field)
    		throws SysException {
    	return getFieldValue(field, true);
    }

    public Object getFieldOrigin(String field) throws SysException {
    	return getFieldOrigin(getField(field));
    }
    
    public Object getFieldValue(@CheckForNull Fields.Field field)
    		throws SysException {
    	return getFieldValue(field, false);
    }

    public Object getFieldValue(String field) throws SysException {
    	return getFieldValue(getField(field));
    }
        
    public void setFieldValue(Fields.Field field, Object value, boolean forReadonly) throws SysException {
        if ( field.isId() ) {
        	return;
        }
    	if ( field.getForm().id != getForm().id ) {
            throw new SysException(Messages.SCMFieldBelongToAnotherForm());
        }
        if ( field.readonly && !forReadonly ) {
        	throw new SysException( String.format(
        		Messages.SCMErrorSetValueForReadonlyField(),
        		form.display(), field.getDisplay()
        	) );
        }
        TYPE type = field.type;
        String name = field.name;
        type.checkObjectType(field, value);
        if ( !data.containsKey(name) ) {
        	data.put(name, new HashMap<String, Object>());
        }
        data.get(name).put(DKEY_VALUE, value);
    }
        
    public void setFieldValue(Fields.Field field, Object value) throws SysException {
    	setFieldValue(field, value, false);
    }
    
    public void setFieldValue(String field, Object value, boolean forReadonly) throws SysException {
        setFieldValue(getField(field), value, forReadonly);
    }
        
    public void setFieldValue(String field, Object value) throws SysException {
        setFieldValue(getField(field), value);
    }
    
    public Record getInstance() {
    	return this;
    }
    
    public void reset() {
    	for ( Map<String, Object> v : data.values()) {
    		v.remove(DKEY_VALUE);
    	}
    }
    
    public boolean valueIsChanged(@CheckForNull Fields.Field field)
    			throws SysException {
    	if ( field.isId() ) {
        	return false;
        }
    	if ( field.getForm().id != getForm().id ) {
            throw new SysException(Messages.SCMFieldBelongToAnotherForm());
        }
    	if ( isNew() ) {
    		return true;
    	}
    	String name = field.name;
    	return data.containsKey(name) && data.get(name).containsKey(DKEY_VALUE);
    }
        
    public void save() throws SysException {
    	Map<String, String> changed 
    		= new HashMap<String, String>();
    	final Map<String, Object> refsChanged
    	 	= new HashMap<String, Object>();
    	getBindAction().doVerifyHook(this);
    	for ( Fields.Field field : form.getFields().items ) {
    		if ( field.required ) {
    			field.type.checkRequired(
    				field,
    				getFieldValue(field)
    			);
    		}
    	}
        for( Fields.Field field : form.getFields().items ) {
        	if ( field.isId() || !valueIsChanged(field) ) {
        		continue;
        	}
        	Object value = field.type.objectToRaw(field, getFieldValue(field));
        	if ( field.type.rawIsArray() ) {
    			refsChanged.put(field.getReferredTables()[0], value);
        	}
        	else {
        		changed.put(field.name, value == null ? null : value.toString());
        	}
        }
        if ( !changed.isEmpty() || !refsChanged.isEmpty() ) {
    		if ( isNew() ) {
    			String values = "", columns = "";
    			for ( Map.Entry<String, String> c : changed.entrySet() ) {
    				columns += (columns.isEmpty() ? "" : ", ")
    						+ PluginImpl.sqlQuote2One(c.getKey());
    				String v = c.getValue();
    				String q = v == null ? "" : "'";
    				values += (values.isEmpty() ? "" : ", ")
    						+ q + (v == null ? "NULL" : v)  + q;
    			}
    			PluginImpl.getInstance().insert(
	            	"INSERT INTO "
    			  + PluginImpl.sqlQuote2One("FORM__" + getForm().name)
	              + " (" + columns + ") VALUES (" + values + ")"
	            , new SQLTransaction() {
	            	 @Override
	            	 public void run(Connection conn, Object... o) throws SysException {
	         			try {
	         				((ResultSet)o[0]).next();
	         				id = ((ResultSet)o[0]).getLong(1);
	         			} catch ( SQLException e ) {
	         				throw new SysException( String.format(
	         				    Messages.SCMErrorSQLRecordIdNotFetched(),
	         				    form.display(), e.getMessage()
	         				) );
	         			}
	         			insertRefs(conn, id, refsChanged);
	            		getBindAction().doPostHook(getInstance());
	            	 }
	            } );
    		} else {
    			String setter = "";
    			for ( Map.Entry<String, String> c : changed.entrySet() ) {
    				String v = c.getValue();
    				String q = v == null ? "" : "'";
    				setter += (setter.isEmpty() ? "" : ", ")
    						+ PluginImpl.sqlQuote2One(c.getKey())
    						+ " = "
    						+ q + (v == null ? "NULL" : v) + q;
    			}
    			PluginImpl.getInstance().update(
	            	"UPDATE " + PluginImpl.sqlQuote2One("FORM__" + getForm().name)
	              + " SET " + setter + " WHERE ID =" + getId() + "" ,
	              new SQLTransaction() {
   	            	 @Override
   	            	 public void run(Connection conn, Object... args) throws SysException {
   	            		 insertRefs(conn, id, refsChanged);
   	            		 getBindAction().doPostHook(getInstance());
   	            	 }
   	            } );
    		}
        } else {
        	getBindAction().doPostHook(this);
    	} 
    }

    private void insertRefs(@CheckForNull Connection conn, long id, Map<String, Object> refsChanged)
    		throws SysException {
		for( Map.Entry<String, Object> ref : refsChanged.entrySet() ) {
			String[] values = null;
			if ( ref.getValue() instanceof long[]  ) {
				values = Utils.longArrToStringArr(
					(long[])ref.getValue()
				);
			} else if ( ref.getValue() instanceof int[] ) {
				values = Utils.intArrToStringArr(
					(int[])ref.getValue()
				);
			} else if ( !(ref.getValue() instanceof String[]) ) {
				continue;
			}
			List<String> inserted = new ArrayList<String>();
			for( int i = 0; i < values.length; i++ ) {
				if ( values[i] == null ) {
					continue;
				}
				String value = String.format(
					"VALUES (%1$d, '%2$s')", 
					id,
					PluginImpl.sqlEscapeEquals(values[i])
				);
				if ( inserted.contains(value) ) {
					continue;
				}
 				inserted.add(values[i]);
			}
			Statement sth = null;
			try {
				sth = conn.createStatement();
				sth.executeUpdate( String.format(
		            "DELETE FROM %1$s WHERE %2$s = %3$d",
	    			PluginImpl.sqlQuote2One(ref.getKey()),
	    			PluginImpl.sqlQuote2One(Fields.REFFIELDS.RECORD.toString()),
	    			id
				) );
				if ( inserted.isEmpty() ) {
					continue;
				}
				sth.executeUpdate( String.format(
		            "INSERT INTO %1$s (%2$s, %3$s) %4$s",
	    			PluginImpl.sqlQuote2One(ref.getKey()),
	    			PluginImpl.sqlQuote2One(Fields.REFFIELDS.RECORD.toString()),
	    			PluginImpl.sqlQuote2One(Fields.REFFIELDS.VALUE.toString()),
	    			Utils.stringJoin(",", inserted.toArray())
				) );
			} catch (SQLException ex) {
				throw new SysException("", ex);
			} finally {
				try {
					if ( sth != null ) {
						sth.close();
					}
				} catch (SQLException ex) {
					
				}
			}
		}
    }
    public void sendEmail(String subject, String message, Object[] addrTos, Object... addrCcs)
    		throws SysException {
    	MailSender mail = new MailSender();
    	mail.setSubject( String.format(
    		Messages.SCMMailRecordSubject(),
    		form.display(),
    		getDisplayName(),
    		Utils.nullAsEmpty(subject)
    	) );
    	String link = getUrl();
    	mail.setBody( String.format( 
    		"<html><body>"
    		+ "<pre>%1$s</pre>"
    		+ "<a href=\"%2$s\">%3$s</a>"
    		+ "</body></html>",
    		StringEscapeUtils.escapeHtml(
    			Utils.nullAsEmpty(message)
    		), link, link
    	) );
    	mail.setContentAsHTML(true);
    	mail.setRecipientsTO(addrTos);
    	mail.setRecipientsCC(addrCcs);
    	mail.send();
    }
    
    public void sendEmail(String subject, String message, List<Object> addrTos, Object... addrCcs)
    		throws SysException {
    	Object[] a2s = addrTos == null
    		? new Object[0] : addrTos.toArray();
    	sendEmail(subject, message, a2s, addrCcs);
    }
    
    public void sendEmail(String subject, String message, Object... addrTos)
    		throws SysException {
    	sendEmail(subject, message, addrTos);
    }
    
	public String getDisplayName() {
		return String.format("%1$08d", getId());
	}

	public String getUrl() {
		return String.format("%1$s/%2$s/%3$d", Home.getUrl(), form.name, getId());
	}
	
	public String getSearchUrl() {
		return ((Long)id).toString();
	}

	public ContextMenu doContextMenu(StaplerRequest arg0, StaplerResponse arg1) throws Exception {
		return null;
	}
	
	public void doFile(StaplerRequest request, StaplerResponse response)
	        throws SysException, ServletException, FileNotFoundException, IOException {
		String path = Utils.trimOrEmpty(
	    	request.getParameter("path")
    	);
		File file = null;
		String name = "";
		Pattern regex = Pattern.compile("^(\\d+)\\/(.+)(\\.\\d+)$");
		Matcher matched = regex.matcher(path);
		if ( matched.find() ) {
			name = matched.group(2);
			file = new File( Utils.stringArrJoin( 
    			File.separator,
	    	    Hudson.getInstance().getRootDir().toString(),
	    	    PluginImpl.DATA_ATTACHES,
	    	    String.format("form-%1$d-%2$s", form.id, matched.group(1)),
	    	    Utils.encodeToBase64(name, "UTF-8") + "." + matched.group(3)
	        ) );
		}
    	if ( file == null || file.isDirectory() || !file.exists() ) {
    		throw new SysException(
    			Messages.SCMErrorAttachmentFileMissing()
    		);
    	}
    	response.setHeader(
    		"Content-Disposition",
    		"attachment; filename=" + name
    	);
    	response.serveFile(
			request,
			new FileInputStream(file),
			file.lastModified(),
			file.length(),
			name
    	);
	}
	
	@RequirePOST
	public void doSave(StaplerRequest request, StaplerResponse response)
	        throws SysException, FormException, IOException {
	    Fields fields = getFields();
		ReqParams parameters
			= new ReqParams(request);
	    for( Fields.Field field : fields.items ) {
	        if ( field.hidden || field.readonly ) {
		        PluginImpl.info( String.format(
		        	"Field (%1$s) changed will be skiped (readonly or hidden).",
		        	field.getDisplay()
		        ) );
	        	continue;
	        }
	        String sv;
	        Fields.TYPE type = field.type;
	        String parameterName = "field_" + field.name;
	        if ( (sv = parameters.get(parameterName)) == null ) {
		        PluginImpl.info( String.format(
		        	"Field (%1$s) changed will be skiped (no saved flag).",
		        	field.getDisplay()
		        ) );
	        	continue;
	        }
	        PluginImpl.info( String.format(
	        	"Field (%1$s) changed will be updated.",
	        	field.getDisplay()
	        ) );
	        if ( type.rawIsFile() ) {
	        	long suffix = new Date().getTime();
	        	Map<String, String> as = new HashMap<String, String>();
	        	for( FileItem fi : parameters.getFileItems(parameterName + "[]")) {
	        		PluginImpl.info( String.format(
	        			"Attachment recieving : field => %1$s, name => %2$s",
	        			field.getDisplay(), fi.getName()
	        		) );
        	    	try {
        	    		String name = fi.getName().trim();
    	        		File file = new File( Utils.stringArrJoin( 
    	        			File.separator,
	        	    	    Hudson.getInstance().getRootDir().toString(),
	        	    	    PluginImpl.DATA_ATTACHES,
	        	    	    String.format("form-%1$d-%2$d", form.id, field.id),
	        	    	    Utils.encodeToBase64(name, "UTF-8") + "." + suffix
	        	        ) );
        	    		file.getParentFile().mkdirs();
        	    		fi.write(file);
        	    		as.put(name.toLowerCase(), name + "." + suffix);
        			} catch (Exception e) {
        				throw new SysException(
        					Messages.SCMFailedToRecieveUpload(),
        					e
        				);
        			}
	        	}
	        	if ( type.rawIsArray() || as.size() == 0 ) {
		        	for ( String str : parameters.getArray(parameterName + "[]")) {
		        		String name;
		        		if ( (name  = str.replace("\\.\\d+$", "")) == str ) {
		        			continue;
		        		}
		        		as.put(name.toLowerCase(), str);
		        	}
	        	}
	        	String[] _as = as.values().toArray(new String[as.size()]);
	        	setFieldValue(field, type.rawIsArray() ? _as : (_as.length> 0 ? _as[0] : null));
	        } else {
	        	String[] vs = Utils.stringSortedUniqueThrowBlank(
	        		type.rawIsArray() 
	        			? parameters.getArray(parameterName + "[]")
	        			: new String[]{sv}
	        	);
	        	Object obj = null;
	            try {
	            	obj = type.rawToObject(
	            		field, type.rawFromArray(vs)
	            	);
	            }
	            catch ( NumberFormatException e ) {
	            	throw new TypeMissingMatchException(
	            		field,
	            		Utils.stringArrJoin(",", vs)
	            	);
	            }
	            if ( type.rawIsArray() ) {
	            	if ( vs.length > 0 && obj == null ) {
	        	    	throw new TypeMissingMatchException(
    	            		field, Utils.stringArrJoin(",", vs)
    	            	);
	            	}
	            } else {
	        	    if ( !Utils.isNullOrBlank(sv) && obj == null ) {
	        	    	throw new TypeMissingMatchException(
    	            		field, sv
    	            	);
	        	    }
	        	}
            	setFieldValue(field, obj);
	        }
	    }
	    save();
	    response.sendRedirect2(getUrl());
	}
}
