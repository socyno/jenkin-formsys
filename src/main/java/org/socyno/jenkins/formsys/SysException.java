
package org.socyno.jenkins.formsys;

import java.io.IOException;

import javax.servlet.ServletException;

import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.model.Failure;
import hudson.util.FormApply;
import static hudson.util.QuotedStringTokenizer.quote;

public class SysException extends Exception implements HttpResponse {
	
	public SysException(String message) {
    	super(message);
    	PluginImpl.warning(this);
    }
   
    public SysException(String message, Throwable cause) {
    	super(message, cause);
    	PluginImpl.warning(cause);
    }
    
	@Override
    public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
        if (FormApply.isApply(req)) {
            FormApply.applyResponse("notificationBar.show(" + quote(getMessage())+ ",notificationBar.ERROR)")
                    .generateResponse(req, rsp, node);
        } else {
            // for now, we can't really use the field name that caused the problem.
            new Failure(getMessage()).generateResponse(req,rsp,node);
        }
    }

	private static final long serialVersionUID = -6733701888490848013L;
}
