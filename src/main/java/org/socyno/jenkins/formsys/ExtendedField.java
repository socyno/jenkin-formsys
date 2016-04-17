package org.socyno.jenkins.formsys;

import java.util.List;
import java.util.ArrayList;

import javax.annotation.CheckForNull;

import org.socyno.jenkins.formsys.Messages;

public class ExtendedField {
	public final Fields.Field field;
	public final ExtendedField extended;
	
	public ExtendedField(@CheckForNull Fields.Field _field)
			throws SysException {
		this(_field, null);
	}
	
	public ExtendedField(@CheckForNull Fields.Field _field, ExtendedField _extended)
			throws SysException {
		field = _field;
		extended = _extended;
		if ( extended != null ) {
			if ( field.referred == null) {
				throw new SysException( String.format(
					Messages.SCMErrorExtendedFieldNotReferred(),
					field.getForm().display(),
					field.getDisplay()
				) );
			}
			if ( field.referred.id != extended.getForm().id ) {
				throw new SysException(  String.format(
					Messages.SCMErrorExtendedFieldUnmatchedReferred(),
					field.getForm().display(),
					field.getDisplay() + " => " + extended.getDisplay()
				) );
			}
		}
	}
	
	public Fields.Field getField() {
		return field;
	}
	
	public Form getForm() {
		return field.getForm();
	}
	
	public String getDisplay() {
		String str = field.getDisplay();
		ExtendedField _extended = extended;
		if ( _extended != null ) {
			str += "." + _extended.getDisplay();
		}
		return str;
	}
	
	public Fields.Field getTarget() {
		Fields.Field target = field;
		ExtendedField _extended = extended;
		if ( _extended != null ) {
			target = _extended.getTarget();
		}
		return target;
	}

	public Form getTargetForm() {
		return getTarget().getForm();
	}
	
	public String getName() {
		String str = field.name;
		ExtendedField _extended = extended;
		if ( _extended != null ) {
			str += "." + _extended.getName();
		}
		return str;
	}
	
	public Fields.TYPE getType() {
		return getTarget().type;
	}
	
	public String[] parseSQLJoins() throws SysException {
		ExtendedField current = this;
		List<String> joins = new ArrayList<String>();
        String column = "", table = "";
		while ( current != null ) {
			if ( table.isEmpty() ) {
				table = "me";
			}
			String[] refs;
			column = current.field.name;
			if ( (refs = current.field.getReferredTables()) == null
					|| refs.length == 0 ) {
				break;
			}
			String jn = table;
			table += "_" + current.field.name;
			column = Fields.SYSFIELD.ID.toString();
			if ( refs.length == 1 ) {
				String on1 = column;
				String on2 = Fields.SYSFIELD.ID.toString();
	        	if ( current.field.type.rawIsArray() ) {
	        		on1 = Fields.SYSFIELD.ID.toString();
	        		on2 = Fields.REFFIELDS.RECORD.toString();
	        		column = Fields.REFFIELDS.VALUE.toString();
	        	}
	        	joins.add( String.format(
	        		"LEFT JOIN %1$s AS %2$s ON %3$s = %4$s",
	        		PluginImpl.sqlQuote2One(refs[0]),
	        		PluginImpl.sqlQuote2One(table),
	        		PluginImpl.sqlQuote2One(table, on2),
	        		PluginImpl.sqlQuote2One(jn, on1)
	        	) );
			} else if ( refs.length > 1 ) {
				String tb1 = jn + "__" + current.field.name;
	        	joins.add( String.format(
	        		"LEFT JOIN %1$s AS %2$s ON %3$s = %4$s",
	        		PluginImpl.sqlQuote2One(refs[0]),
	        		PluginImpl.sqlQuote2One(tb1),
	        		PluginImpl.sqlQuote2One(tb1, Fields.REFFIELDS.RECORD.toString()),
	        		PluginImpl.sqlQuote2One(jn, Fields.SYSFIELD.ID.toString())
	        	) );
	        	joins.add( String.format(
	        		"LEFT JOIN %1$s AS %2$s ON %3$s = %4$s",
	        		PluginImpl.sqlQuote2One(refs[1]),
	        		PluginImpl.sqlQuote2One(table),
	        		PluginImpl.sqlQuote2One(table, Fields.SYSFIELD.ID.toString()),
	        		PluginImpl.sqlQuote2One(tb1, Fields.REFFIELDS.VALUE.toString())
		        ) );
			}
        	current = current.extended;
		}
        joins.add(0, PluginImpl.sqlQuote2One(table, column));
		String [] result = joins.toArray(new String[joins.size()]);
		PluginImpl.info( String.format(
			"Parsed joins %1$s : %2$s",
			this.getName(),
			Utils.stringJoin(", ", ((Object[])result))
		) );
		return result;
	}
}