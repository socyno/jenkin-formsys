
package org.socyno.jenkins.formsys

import org.socyno.jenkins.formsys.Form
import org.socyno.jenkins.formsys.Messages

def l = namespace(lib.LayoutTagLib);
l.layout(title: _(Messages.PluginDisplayName())) {
    l.side_panel(_(Messages.PluginDisplayName())) {
        ["DPM_MESGQUEUE", "DPM_DATABASES", "DPM_APPLICATIONS",
         "DPM_PARAMETERS", "DPM_DATASOURCES", "DPM_OTHERS"].each() { fr ->
            def form = new Form(fr);
            l.task(
    			icon: "images/24x24/new-package.png",
                href: sprintf("%s/%s/%s",
                    rootURL,
                    my.getUrlName(),
                    form.getName()
                ),
               	title: form.getDisplay()
         	)
     	}
     	hr( class:"bill99scm-block bill99scm-width100" ) {}
     	["APPLICATIONS", "DB_SERVICES", "DB_SCHEMAS", "MQ_SERVICES", "PRODUCTS"].each() { fr ->
            def form = new Form(fr);
            l.task(
                icon: "images/24x24/new-package.png",
                href: sprintf("%s/%s/%s",
                    rootURL,
                    my.getUrlName(),
                    form.getName()
                ),
                title: form.getDisplay()
            )
        }
    }

    l.main_panel() {
        h1(_(""));
    }
}
