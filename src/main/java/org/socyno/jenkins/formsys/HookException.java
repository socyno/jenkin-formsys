
package org.socyno.jenkins.formsys;

import org.socyno.jenkins.formsys.Messages;

public class HookException extends SysException {
  
	public enum HOOK { DEFAULT, CONDITION, PREPARE, POSTSAVE, VERIFY };
	public enum TYPE { OUTPUT, RUNTIME };
	    
    public HookException( HOOK hook, Fields.Field field, TYPE type ) {
    	this(hook, field.getForm(), field, type, null);
    }
    
    public HookException( HOOK hook, Fields.Field field, TYPE type, Exception e ) {
    	this(hook, field.getForm(), field, type, e);
    }
    
    public HookException( HOOK hook, Form form, TYPE type ) {
        this(hook, form, null, type, null);
    }
    
    public HookException( HOOK hook, Form form, TYPE type, Exception e ) {
        this(hook, form, null, type, e);
    }
    
    public HookException( HOOK hook, Form form, Fields.Field field, TYPE type ) {
        this(hook, form, field, type, null);
    }
    
    public HookException( HOOK hook, Form form, Fields.Field field, TYPE type, Exception e ) {
        super( String.format(
        	Messages.SCMFailedToRunHook(),
        	hook.name(),
        	form.display()
        	+ "/" + (field == null ? "" : field.getDisplay())
        	+ "/" + (type == null ? "" : type.name())
        	+ "/" + (e == null ? "" : e.getMessage())
        ) );
    }
    
    private static final long serialVersionUID = 6841510246360284493L;
}
