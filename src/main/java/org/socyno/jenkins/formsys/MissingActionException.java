package org.socyno.jenkins.formsys;

import org.socyno.jenkins.formsys.Messages;

public class MissingActionException extends SysException {
	
	public MissingActionException(Form form, String name) {
		super( String.format(
			Messages.SCMErrorMissingAction(),
			form.display(), name
		) );
	}
}
