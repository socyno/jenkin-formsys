package org.socyno.jenkins.formsys;

import javax.annotation.CheckForNull;

import org.socyno.jenkins.formsys.Messages;

public class SearchOrder {
	public final ExtendedField field;
	public final ORDER order;
	public static enum ORDER {
		ASC {
			@Override
			public String display() {
				return Messages.SCMSearchOrderAscDisplay();
			}
		},
		DESC {
			@Override
			public String display() {
				return Messages.SCMSearchOrderDescDisplay();
			}
		};
		abstract public String display();
	};
	public SearchOrder(
			@CheckForNull
			ExtendedField _field
		) throws SysException {
		this(_field, ORDER.ASC);
	}
	
	public SearchOrder(
			@CheckForNull
			ExtendedField _field,
			@CheckForNull
			ORDER _order
		) throws SysException {
		field = _field;
		order = _order;
	}
}