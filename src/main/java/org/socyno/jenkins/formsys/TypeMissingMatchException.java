package org.socyno.jenkins.formsys;

import org.socyno.jenkins.formsys.Messages;

public class TypeMissingMatchException extends SysException {
	public final Fields.Field field;
	private static final long serialVersionUID = 3502381397646193806L;
	
	public TypeMissingMatchException(
			Fields.Field field,
			Object object
	) {
		super( String.format(
			Messages.SCMFFieldTypeNotMatch(),
			field.getDisplay(),
			field.type.display(),
			" : " + Utils.nullAsEmpty(object)
		) );
		this.field = field;
	}	
}
