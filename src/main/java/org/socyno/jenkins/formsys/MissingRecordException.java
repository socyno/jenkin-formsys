
package org.socyno.jenkins.formsys;

import org.socyno.jenkins.formsys.Messages;

public class MissingRecordException extends SysException {
    
    public MissingRecordException(Form form, long id) {
    	this(form, id + "");
    }
    

    public MissingRecordException(Form form, String id) {
        super(String.format(Messages.SCMErrorMissingRecord(),form.name ,id));
	}
    
    private static final long serialVersionUID = 6841510246360284493L;
}
