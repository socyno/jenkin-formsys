package org.socyno.jenkins.formsys;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.kohsuke.stapler.StaplerRequest;

import org.socyno.jenkins.formsys.Messages;

class ReqParams {
    private final List<Object> parameters = new ArrayList<Object>();
    
    @SuppressWarnings("unchecked")
	public ReqParams(StaplerRequest request) throws SysException {
    	try {
    		if ( Utils.trimOrEmpty(request.getContentType())
					.equalsIgnoreCase("application/x-www-form-urlencoded")  ) {
				Map<String, Object> m = request.getParameterMap();
				for ( Map.Entry<String, Object> e : m.entrySet() ) {
					String name = e.getKey();
					Object value = e.getValue();
					if ( value instanceof String[] ){
			            String[] values = (String[])value;
			            for ( int i = 0; i <values.length; i++) {
				            parameters.add( new String[] {
				            	name,
				            	Utils.trimOrEmpty(values[i])
				            } );
			            }
			        } else if ( value instanceof String ) {
			        	parameters.add( new String[] {
			        		name,
			        		Utils.trimOrEmpty((String)value)
			        	} );
			        }
				}
    		} else {
				for ( FileItem f : (List<FileItem>)new ServletFileUpload(new DiskFileItemFactory())
						.parseRequest(request) ) {
					if ( f.isFormField() ) {
						parameters.add( new String[] {
							f.getFieldName(),
							Utils.trimOrEmpty(f.getString("UTF-8"))
						} );
					} else if ( !(Utils.nullAsEmpty(f.getName()).isEmpty()) ) {
						parameters.add(f);
					}
				}
			}
		} catch ( Exception e ) {
			throw new SysException(Messages.SCMFailedToParseReqParamters(), e);
		}
    }
    
    public String get(String name) {
    	for ( Object p : parameters ) {
    		if ( p instanceof String[] && ((String[])p)[0].equalsIgnoreCase(name) ) {
    			return ((String[])p)[1];
    		}
    	}
    	return null;
    }

    public String[] getArray(String name) {
    	List<String> v = new ArrayList<String>();
    	for ( Object p : parameters ) {
    		if ( p instanceof String[] && ((String[])p)[0].equalsIgnoreCase(name) ) {
    			String _v;
    			if ( !(_v = ((String[])p)[1]).isEmpty() ) {
    				v.add(_v);
    			}
    		}
    	}
    	return v.toArray(new String[v.size()]);
    }

    public FileItem getFileItem(String name) {
    	for ( Object p : parameters ) {
    		if ( p instanceof FileItem
    		  && Utils.trimOrEmpty(((FileItem)p).getFieldName()).equalsIgnoreCase(name) ) {
    			return ((FileItem)p);
    		}
    	}
    	return null;
    }

    public FileItem[] getFileItems(String name) {
    	List<FileItem> v = new ArrayList<FileItem>();
    	for ( Object p : parameters ) {
    		if ( p instanceof FileItem
    		  && Utils.trimOrEmpty(((FileItem)p).getFieldName()).equalsIgnoreCase(name) ) {
    			v.add(((FileItem)p));
    		}
    	}
    	return v.toArray(new FileItem[v.size()]);
    }
}