package org.socyno.jenkins.formsys;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.annotation.CheckForNull;

import org.socyno.jenkins.formsys.Fields.Field;

import org.socyno.jenkins.formsys.Messages;

public class SearchFilter {
	public final ExtendedField field;
	public final Fields.OPERATORS operator;
	public final List<String> values = new ArrayList<String>();
	
	public SearchFilter(
			@CheckForNull
			ExtendedField _field,
			@CheckForNull
			Fields.OPERATORS _operator
		) throws SysException {
		this(_field, _operator, null);
	}
	
	public SearchFilter(
			@CheckForNull
			ExtendedField _field,
			@CheckForNull
			Fields.OPERATORS _operator,
			String[] _values
		) throws SysException {
		field = _field;
		operator = _operator;
		valuesAdd(_values);
	}
	
	public ExtendedField getField() {
		return field;
	}
	
	public String[] getValues() {
		return values.toArray(new String[values.size()]);
	}
	
	public String[] getValues(boolean parsed) throws SysException {
		List<String> parsedVal;
		Field target = field.getTarget();
		Fields.TYPE type = target.type;
		if ( type.rawIsInteger() ) {
			parsedVal = new ArrayList<String>();
			for ( String v : values ) {
				Object date; Long longVal = null;
				if ( type.rawIsDateTime() && (date = type.rawToObject(target, v)) != null ) {
					longVal = (long)(((Date)date).getTime() / 1000);
				} else {
					longVal = Utils.parseLongObj(v, null);
				}
				if ( longVal == null ) {
					throw new SysException( String.format(
						Messages.SCMErrorSearchFilterRequireInteger(),
						field.getForm().display(),
						field.getDisplay()
					) );
				}
				parsedVal.add(((Long)longVal).toString());
			}
		} else {
			parsedVal = values;
		}
		return Utils.stringSortedUnique( parsedVal.toArray(
			new String[parsedVal.size()]
		) );
	}
	
	public void valuesAdd (String... vs) throws SysException {
		if ( vs == null || !operator.valueAllowed() ) {
			return;
		}
		values.addAll(Arrays.asList(vs));
	}
	
	public void valuesClear() {
		values.clear();
	}
		
	public String parseCondition(@CheckForNull String column) throws SysException {
		String condition = "";
		String[] values = getValues(true);
        if ( operator.equals(Fields.OPERATORS.EMPTY) ) {
       	  	condition = String.format("%1$s IS NULL OR %1$s = ''", column);
        } else if ( operator.equals(Fields.OPERATORS.NOT_EMPTY) ) {
        	condition = String.format("%1$s IS NOT NULL AND %1$s != ''", column);
        } else if ( operator.equals(Fields.OPERATORS.EQUALS) ) {
    		for ( String v : values ) {
    			condition += (condition.isEmpty() ? "" : " OR ") + String.format(
    				"%1$s = '%2$s'", column, PluginImpl.sqlEscapeEquals(v)
    			);
    		}
        } else if ( operator.equals(Fields.OPERATORS.NOT_EQUALS) ) {
    		for ( String v : values ) {
    			condition += (condition.isEmpty() ? "" : " OR ") + String.format(
    				"%1$s != '%2$s'", column, PluginImpl.sqlEscapeEquals(v)
    			);
    		}
        } else if ( operator.equals(Fields.OPERATORS.LESS) ) {
        	Long max = Utils.longGetMax(
        		Utils.stringArrToLongArr(values)
        	);
        	if ( max != null ) {
	        	condition = column + " < " + max;
        	}
        } else if ( operator.equals(Fields.OPERATORS.NOT_LESS) ) {
        	Long min = Utils.longGetMin(
            	Utils.stringArrToLongArr(values)
        	);
        	if ( min != null ) {
	        	condition = column + " >= " + min;
        	}
        } else if ( operator.equals(Fields.OPERATORS.GREATER) ) {
        	Long min = Utils.longGetMin(
            	Utils.stringArrToLongArr(values)
        	);
        	if ( min != null ) {
	        	condition = column + " > " + min;
        	}
        } else if ( operator.equals(Fields.OPERATORS.NOT_GREATER) ) {
        	Long max = Utils.longGetMax(
        		Utils.stringArrToLongArr(values)
        	);
        	if ( max != null ) {
	        	condition = column + " <= " + max;
        	}
        } else if ( operator.equals(Fields.OPERATORS.CONTAINS) ) {
        	for( String v : values ) {
    			condition += (condition.isEmpty() ? "" : " OR ") + String.format(
    				"%1$s LIKE '%%%2$s%%'", column, PluginImpl.sqlEscape(v) );
        	}
        } else {
        	for( String v : values ) {
    			condition += (condition.isEmpty() ? "" : " OR ") + String.format(
    			    "%1$s NOT LIKE '%%%2$s%%'", column, PluginImpl.sqlEscape(v)
  			    );
        	}
        } 
		return condition;
	}
}