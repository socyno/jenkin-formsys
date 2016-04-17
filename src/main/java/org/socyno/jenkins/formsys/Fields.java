package org.socyno.jenkins.formsys;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import javax.annotation.CheckForNull;

public class Fields {
	public final Form form;
    public final List<Field> items;
    private Map<String, List<Option>> options = null;
    
    public static enum REFFIELDS {
    	RECORD, VALUE;
    }
    
    public static enum SYSFIELD {
    	ID {
    		@Override
        	public TYPE type() {
        		return TYPE.INTEGER;
        	}
    		@Override
    		public String getDisplay() {
    			return Messages.SCMSysFieldIdDisplay();
    		}
    	}, STATUS {
    		@Override
        	public TYPE type() {
        		return TYPE.STRING;
        	}
    		@Override
        	public boolean statusOnly() {
        		return true;
        	}
    		@Override
    		public String getDisplay() {
    			return Messages.SCMSysFieldStatusDisplay();
    		}
    	}, LASTMODIFIED {
    		@Override
        	public TYPE type() {
        		return TYPE.INTEGER;
        	}
    		@Override
    		public String getDisplay() {
    			return Messages.SCMSysFieldLastModifiedDisplay();
    		}
    	}, LASTMODIFIER {
    		@Override
        	public TYPE type() {
        		return TYPE.REFERENCE;
        	}
    		@Override
        	public Form referred() throws SysException {
        		return Form.get("USERS");
        	}
    		@Override
    		public String getDisplay() {
    			return Messages.SCMSysFieldLastModifierDisplay();
    		}
    	};
    	
    	public String getDisplay() {
    		return this.toString();
    	}
    	
    	public Field get(Fields p) throws SysException {
    		return p.new Field(
    			BigInteger.valueOf(ordinal()).negate().longValue(),
    			toString(),
    			type(),
    			referred(),
    			referredColumns()
    		) {
    			@Override
    			public String getDisplay() {
    				return SYSFIELD.this.getDisplay();
    			}
    		};
    	}
    	
    	public Form referred() throws SysException {
    		return null;
    	}
    	
    	public boolean statusOnly() {
    		return false;
    	}
    	
    	public String[] referredColumns() {
    		return null;
    	}

    	public TYPE type() {
    		return TYPE.STRING;
    	}
    }
        
    public enum OPERATORS {
    	EMPTY {
			@Override
	    	public boolean valueAllowed() {
	    		return false;
	    	}
			@Override
			public String display() {
				return Messages.SCMDBCompareOperatorNull();
			}
		},
    	NOT_EMPTY {
			@Override
			public String display() {
				return Messages.SCMDBCompareOperatorNotNull();
			}
			@Override
	    	public boolean valueAllowed() {
	    		return false;
	    	}
		},
    	EQUALS {
			@Override
			public String display() {
				return Messages.SCMDBCompareOperatorEquals();
			}
		},
    	NOT_EQUALS {
			@Override
			public String display() {
				return Messages.SCMDBCompareOperatorNotEquals();
			}
		},
    	GREATER {
			@Override
			public String display() {
				return Messages.SCMDBCompareOperatorGreater();
			}
		},
    	NOT_GREATER {
			@Override
			public String display() {
				return Messages.SCMDBCompareOperatorNotGreater();
			}
		},
    	LESS {
			@Override
			public String display() {
				return Messages.SCMDBCompareOperatorLess();
			}
		},
    	NOT_LESS {
			@Override
			public String display() {
				return Messages.SCMDBCompareOperatorNotLess();
			}
		},
    	CONTAINS {
			@Override
			public String display() {
				return Messages.SCMDBCompareOperatorContains();
			}
		},
    	NOT_CONTAINS {
			@Override
			public String display() {
				return Messages.SCMDBCompareOperatorNotContains();
			}
		};
    	abstract public String display();
    	public boolean valueAllowed() {
    		return true;
    	}
    }
    
    public static enum TYPE {
        TEXT {
        	@Override
            public boolean rawIsInteger() {
                return false;
            }
        	@Override
            public boolean rawIsArray() {
                return false;
            }
        	@Override
            public boolean rawIsLongText() {
                return true;
            }
            @Override
            public String display() {
                return Messages.SCMFieldTypeTextDisplay();
            }
			@Override
			public OPERATORS[] allowedOperators() {
				return new OPERATORS[] {
					OPERATORS.EMPTY,
					OPERATORS.NOT_EMPTY,
					OPERATORS.CONTAINS,
					OPERATORS.NOT_CONTAINS
				};
			}
			@Override
			public Object objectToRaw(Field field, Object object)
					throws SysException {
				return object;
			}
			@Override
			public void checkObjectType(Field field, Object object)
					throws TypeMissingMatchException {
				return;
			}
        },
        STRING {
        	@Override
            public boolean rawIsInteger() {
                return false;
            }
        	@Override
            public boolean rawIsArray() {
                return false;
            }
            @Override
            public String display() {
                return Messages.SCMFieldTypeStringDisplay();
            }
			@Override
			public OPERATORS[] allowedOperators() {
				return new OPERATORS[] {
					OPERATORS.EMPTY,
					OPERATORS.NOT_EMPTY,
					OPERATORS.EQUALS,
					OPERATORS.NOT_EQUALS,
					OPERATORS.CONTAINS,
					OPERATORS.NOT_CONTAINS
				};
			}
			@Override
			public Object objectToRaw(Field field, Object object)
					throws SysException {
				return object;
			}
			@Override
			public void checkObjectType(Field field, Object object)
					throws TypeMissingMatchException {
				return;
			}
        },
        INTEGER {
        	@Override
            public boolean rawIsInteger() {
                return true;
            }
        	@Override
            public boolean rawIsArray() {
                return false;
            }
            @Override
            public String display(Object object, Field field)
            		throws SysException {
            	Object raw = objectToRaw(field, object);
                return raw == null ? "" : raw.toString();
            }
            @Override
            public String display() {
                return Messages.SCMFieldTypeIntegerDisplay();
            }
            @Override
            public void checkObjectType(Field field, Object object)
                    throws TypeMissingMatchException {
                if ( object != null && !(object instanceof Long || object instanceof Integer ) ) {
                    throw new TypeMissingMatchException(
                    	field, object.toString()
                    );
                }
            }
			@Override
			public OPERATORS[] allowedOperators() {
				return new OPERATORS[] {
					OPERATORS.EMPTY,
					OPERATORS.NOT_EMPTY,
					OPERATORS.EQUALS,
					OPERATORS.NOT_EQUALS,
					OPERATORS.LESS,
					OPERATORS.NOT_LESS,
					OPERATORS.GREATER,
					OPERATORS.NOT_GREATER
				};
			}
			@Override
			public Object objectToRaw(Field field, Object object) throws SysException {
				return object;
			}
        },
        INTEGERS {
        	@Override
            public boolean rawIsInteger() {
                return true;
            }
        	@Override
            public boolean rawIsArray() {
                return true;
            }
            @Override
            public String display(Object object, Field field)
            		throws SysException {
            	Object raw = objectToRaw(field, object);
            	if ( raw instanceof int[] ) {
            		return Utils.stringArrJoin(
            			",",
            			Utils.intArrToStringArr((int[])raw)
            		);
            	} else if ( raw instanceof long[] ) {
            		return Utils.stringArrJoin(
            			",",
            			Utils.longArrToStringArr((long[])raw)
            		);
            	}
                return null;
            }
            @Override
            public String display() {
                return Messages.SCMFieldTypeIntegersDisplay();
            }
            @Override
            public void checkObjectType(Field field, Object object)
                    throws TypeMissingMatchException {
                if ( object != null && !(object instanceof long[] || object instanceof int[] ) ) {
                    throw new TypeMissingMatchException(
                    	field, object.toString()
                    );
                }
            }
			@Override
			public OPERATORS[] allowedOperators() {
				return new OPERATORS[] {
					OPERATORS.EMPTY,
					OPERATORS.NOT_EMPTY,
					OPERATORS.EQUALS,
					OPERATORS.NOT_EQUALS,
					OPERATORS.LESS,
					OPERATORS.NOT_LESS,
					OPERATORS.GREATER,
					OPERATORS.NOT_GREATER
				};
			}
			@Override
			public Object objectToRaw(Field field, Object object) throws SysException {
				return object;
			}
        },
        REFERENCE {
        	@Override
            public boolean rawIsInteger() {
                return true;
            }
        	@Override
            public boolean rawIsArray() {
                return false;
            }
        	@Override
            public boolean rawIsReference() {
            	return true;
            }
            @Override
            public Object rawToObject(Field field, Object raw)
            		throws SysException {
            	if ( raw instanceof Long && (Long)raw > 0 ) {
            		return Record.get(field.referred, (Long)raw);
            	}
                return null;
            }
            @Override
            public Object objectToRaw(Field field, Object object) {
            	if ( object instanceof Record ) {
            		return ((Record)object).getId();
            	}
                return null;
            }
            @Override
            public String display(Object object, Field field) {
            	Object s;
            	if ( (s = objectToRaw(field, object)) == null ) {
            		return "";
            	}
            	return String.format("%1$08d", ((Long)s).longValue());
            }
            @Override
            public String display() {
                return Messages.SCMFieldTypeReferenceDisplay();
            }
            @Override
            public void checkObjectType(Field field, Object object)
                    throws TypeMissingMatchException {
            	if ( object == null ) {
            		return;
            	}
        		if ( !(object instanceof Record) ) {
        			throw new TypeMissingMatchException(
        				field, object.toString()
        			);
        		}
                if ( ((Record)object).form.id != field.referred.id ) {
                    throw new TypeMissingMatchException(
                    	field, "not in referred form"
                    );
                }
            }
			@Override
			public OPERATORS[] allowedOperators() {
				return new OPERATORS[] {
					OPERATORS.EMPTY,
					OPERATORS.NOT_EMPTY,
				};
			}
        },
        REFERENCES {
        	@Override
            public boolean rawIsInteger() {
                return true;
            }
        	@Override
            public boolean rawIsArray() {
                return true;
            }
        	@Override
            public boolean rawIsReference() {
            	return true;
            }
            @Override
            public Object rawToObject(Field field, Object raw)
            		throws SysException {
        		if ( raw instanceof long[] ) {
        			return Record.get(field.referred, (long[])raw);
        		}
                return null;
            }
            @Override
            public Object objectToRaw(Field field, Object object) {
        		if ( object instanceof Record[] ) {
        			Record[] v = (Record[])object;
        			List<Long> r = new ArrayList<Long>();
        			for ( int i = 0; i < v.length; i++  ) {
        				if ( v[i] == null || r.contains(v[i].getId())) {
        					continue;
        				}
        				r.add(v[i].getId());
        			}
        			long[] s = new long[r.size()];
        			for ( int i = 0; i < s.length; i++  ) {
        				s[i] = r.get(i).longValue();
        			}
        			return s;
        		}
                return null;
            }
            @Override
            public String display(Object object, Field field) {
            	Object s;
            	if ( (s = objectToRaw(field, object)) == null ) {
            		return "";
            	}
            	String r = "";
            	for ( int i = 0; i < ((long[])s).length; i++ ) {
            		if ( !r.isEmpty() ) {
            			r += ",";
            		}
            		r += String.format("%1$08d", ((long[])s)[i]);
                }
                return r;
            }
            @Override
            public String display() {
                return Messages.SCMFieldTypeReferenceDisplay();
            }
            @Override
            public void checkObjectType(Field field, Object object)
                    throws TypeMissingMatchException {
            	if ( object == null ) {
            		return;
            	}
        		if ( !(object instanceof Record[]) ) {
        			throw new TypeMissingMatchException(
        				field, object.toString()
        			);
        		}
        		Record[] s = (Record[])object;
            	for ( int i = 0; i < s.length; i++ ) {
            		if ( s[i] == null ) {
            			continue;
            		}
                    if ( s[i].form.id != field.referred.id ) {
                        throw new TypeMissingMatchException(
                        	field, "not in referred form"
                        );
                    }
            	}
            }
			@Override
			public OPERATORS[] allowedOperators() {
				return new OPERATORS[] {
					OPERATORS.EMPTY,
					OPERATORS.NOT_EMPTY,
				};
			}
        },
        ATTACHMENT {
        	@Override
            public boolean rawIsFile() {
            	return true;
            }
        	@Override
            public boolean rawIsInteger() {
                return false;
            }
        	@Override
            public boolean rawIsArray() {
                return false;
            }
            @Override
            public Object rawToObject(Field field, Object raw) throws SysException {
                return raw;
            }
            @Override
            public Object objectToRaw(Field field, Object object)
            		throws SysException {
                return object;
            }
            @Override
            public String display(Object object, Field field) {
            	if ( object != null ) {
            		return object.toString().replace("\\.\\d+$", "");
            	}
                return "";
            }
            @Override
            public String display() {
                return Messages.SCMFieldTypeAttachmentDisplay();
            }
            @Override
            public void checkObjectType(Field field, Object object)
                    throws TypeMissingMatchException {
                return;
            }
			@Override
			public OPERATORS[] allowedOperators() {
				return new OPERATORS[] {
					OPERATORS.EMPTY,
					OPERATORS.NOT_EMPTY,
					OPERATORS.EQUALS,
					OPERATORS.NOT_EQUALS,
					OPERATORS.CONTAINS,
					OPERATORS.NOT_CONTAINS,
				};
			}
        },
        ATTACHMENTS {
        	@Override
            public boolean rawIsFile() {
            	return true;
            }
        	@Override
            public boolean rawIsInteger() {
                return true;
            }
        	@Override
            public boolean rawIsArray() {
                return true;
            }
            @Override
            public Object rawToObject(Field field, Object raw)
            		throws SysException {
                return raw;
            }
            @Override
            public Object objectToRaw(Field field, Object object)
            		throws SysException {
                return object;
            }
            @Override
            public String display(Object object, Field field)
            		throws SysException {
                if ( object instanceof String[] ) {
                	String[] as = (String[])object;
                	List<String> sa = new ArrayList<String>();
                	for ( int i = 0; i < as.length; i++ ) {
                		if ( as[i] != null ) {
                			sa.add(as[i].replace("\\.\\d+$", ""));
                		}
                	}
                    return Utils.stringJoin(",", sa.toArray());
                }
                return "";
            }
            @Override
            public String display() {
                return Messages.SCMFieldTypeAttachmentsDisplay();
            }
            @Override
            public void checkObjectType(Field field, Object object)
                    throws TypeMissingMatchException {
                if ( object != null && !(object instanceof String[]) ) {
                    throw new TypeMissingMatchException(
                    	field, object.toString()
                    );
                }
            }
			@Override
			public OPERATORS[] allowedOperators() {
				return new OPERATORS[] {
					OPERATORS.EMPTY,
					OPERATORS.NOT_EMPTY
				};
			}
        },
        DATETIME {
        	@Override
            public boolean rawIsInteger() {
                return true;
            }
        	@Override
            public boolean rawIsArray() {
                return false;
            }
        	@Override
            public boolean rawIsDateTime() {
                return true;
            }
            @Override
            public Object rawToObject(Field field, Object raw)
            		throws SysException {
                if ( raw instanceof Long ) {
                	return new Date((Long)raw*1000);
                }
                Long date;
                if ( (date = Utils.parseLongObj((String)raw, null)) != null ) {
                	return new Date((Long)date*1000);
                }
                return Utils.parseDateTime(Utils.nullAsEmpty(raw));
            }
            @Override
            public Object objectToRaw(Field field, Object object)
            		throws SysException {
                if ( object instanceof Date ) {
                    return (((Date)object).getTime() / 1000);
                }
                return null;
            }
            @Override
            public String display(Object object, Field field) {
                if ( object instanceof Date ) {
                	return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                				.format((Date)object);
                }
                return "";
            }
            @Override
            public String display() {
                return Messages.SCMFieldTypeDateTimeDisplay();
            }
            @Override
            public void checkObjectType(Field field, Object object)
                    throws TypeMissingMatchException {
                if ( object != null && !(object instanceof Date) ) {
                    throw new TypeMissingMatchException(
                    	field, object.toString()
                    );
                }
            }
			@Override
			public OPERATORS[] allowedOperators() {
				return new OPERATORS[] {
					OPERATORS.EMPTY,
					OPERATORS.NOT_EMPTY,
					OPERATORS.LESS,
					OPERATORS.NOT_LESS,
					OPERATORS.GREATER,
					OPERATORS.NOT_GREATER
				};
			}
        };
        public Object rawToObject( Field field, Object raw )
        		throws SysException {
        	return raw;
        }
        public String display(Object object) throws SysException {
            return display(object, null);
        }
        public String display(Object object, Field field) throws SysException {
        	return object == null ? "" : object.toString();
        }
        public String display() {
            return this.toString();
        }
        public void checkRequired(Field field, Object object )
            throws SysException {
    		Object raw = objectToRaw(field, object);
    		if ( raw == null || (raw instanceof String && raw.toString().trim().isEmpty() ) ) {
    			throw new SysException( String.format(
    				Messages.SCMErrorRequiredFieldNoValue(),
    				field.getForm().display(),
    				field.getDisplay()
    			) );
    		}
        }
        public boolean rawIsFile() {
        	return false;
        }
        public boolean rawIsLongText() {
        	return false;
        }
        public boolean rawIsDateTime() {
        	return false;
        }
        public boolean rawIsReference() {
        	return false;
        }
        abstract public boolean rawIsArray();
        abstract public boolean rawIsInteger();
        abstract public OPERATORS[] allowedOperators();
        abstract public void checkObjectType( Field field, Object object )
                throws TypeMissingMatchException;
        abstract public Object objectToRaw(Field field, Object object )
        		throws SysException;
        public Object rawFromArray( String... raw ) {
        	if ( raw == null || raw.length == 0 ) {
            	return null;
        	}
        	int accessLength = raw.length;
        	if ( !rawIsArray() ) {
        		accessLength = 1;
        	}
        	List<Object> values = new ArrayList<Object>();
        	for ( int i = 0; i < accessLength; i++ ) {
         		String o = raw[i];
         		if ( o == null || (o = o.trim()).isEmpty() ) {
         			continue;
         		}
        		if ( rawIsInteger() ) {
        			long x = Utils.parseLongObj(o);
        			if ( !values.contains(x) ) {
        				values.add(x);
        			}
             	} else {
             		if ( !values.contains(o) ) {
             			values.add(o);
             		}
             	}
        	}
        	if ( values.size() <= 0 ) {
        		return null;
        	}
        	if ( !rawIsArray() ) {
        		return rawIsInteger() ? ((Long)values.get(0)).longValue()
        				: values.get(0);
        	} else if ( !rawIsInteger() ) {
        		return values.toArray(new String[values.size()]);
        	} else {
        		long[] x = new long[values.size()];
        		for ( int i = 0; i < x.length; i++ ) {
        			x[i] = ((Long)values.get(0)).longValue();
        		}
        		return x;
        	}
        }
    }

    public Fields (final @CheckForNull Form form) throws SysException {
        this.form = form;
        final List<Field> itemsX = new ArrayList<Field>();
        PluginImpl.getInstance().query(
            "SELECT " + Utils.stringJoin( ",", PluginImpl.sqlQuote(
            	"ID", "NAME", "TYPE", "REFERRED", "DISPLAY", "HELPTEXT", "DESCRIPTION",
            	"DEFAULT", "VALCHECK", "VALCHANGED", "HIDDEN", "READONLY", "REQUIRED",
            	"BLOCKED", "SHOWTITLE","SELECTINPUT", "REFERREDCOLUMNS"
            ) ) + String.format(
            	" FROM %1$s WHERE(%2$s='%3$s') ORDER BY `ORDER`",
            	PluginImpl.sqlQuote2One("FIELDS"),
            	PluginImpl.sqlQuote2One("FORM"),
            	form.name,
            	PluginImpl.sqlQuote2One("ORDER")
            ),
            new SQLTransaction() { @Override public void run(Connection conn, Object... o) throws SysException {
       		  	try {
       		  		ResultSet rs = (ResultSet)o[0];
	                while ( rs.next() ) {
	                	long id = rs.getLong(1);
	                	String name = rs.getString(2);
	                    TYPE type = TYPE.STRING;
                		try {
                            type = TYPE.valueOf(TYPE.class, Utils.trimOrEmpty(rs.getString(3)));
                        } catch( Exception e ) { }
                		Form form = type.rawIsReference() ? null : Form.get(rs.getString(4));
                		String[] columns = Utils.splitThrowEmpty(",", rs.getString(17), true);
	                	Field field = new Field (id, name, type, form, columns);
	                	field.display = rs.getString(5);
	                	field.helpText = rs.getString(6);
	                	field.description = rs.getString(7);
	                	field.hookDefault = rs.getString(8);
	                	field.hookChecker = rs.getString(9);
	                	field.hookChanged = rs.getString(10);
	                	field.hidden = rs.getBoolean(11);
	                	field.readonly = rs.getBoolean(12);
	                	field.required = rs.getBoolean(13);
	                	field.displayBlocked = rs.getBoolean(14);
	                	field.showTitle = rs.getBoolean(15);
	                	field.useSelectInput = rs.getBoolean(16);
	                	itemsX.add(field);
	                }
       		  	} catch ( SQLException e ) {
       		  		throw new SysException(
       		  		    Messages.SCMFailedToInitializeField(),
   		          	    e
   		            );
   		        }
        } } );
        for ( SYSFIELD v : SYSFIELD.values() ) {
        	if ( !form.withStatus && v.statusOnly() ) {
        		continue;
        	}
        	itemsX.add(v.get(this));
        }
        this.items = Collections.unmodifiableList(itemsX);
    }

    public Field get(String name) throws SysException {
        return get(name, false);
    }
    
    public Field get(String name, boolean missingAsNull) throws SysException {
        for ( Field f : this.items ) {
            if (f.name.equalsIgnoreCase(name) ) {
                return f;
            }
        }
    	if ( !missingAsNull ) {
    		throw new SysException( String.format(
    			Messages.SCMErrorFieldNotDefined(),
    			form.display(), name
    		) );
    	}
    	return null;
    }
    
    public List<ExtendedField> getExtended(boolean ex) throws SysException {
    	List<ExtendedField> extended
    		= new ArrayList<ExtendedField>();
    	for ( int i = 0; i < items.size(); i++ ) {
    		Field field = items.get(i);
			extended.add(new ExtendedField(field, null));
    		if ( ex && field.referred != null ) {
    			for ( ExtendedField e : field.referred.getFields().getExtended(true)) {
    				extended.add(new ExtendedField(field, e));
    			}
    		}
    	}
    	return extended;
    }
    
    private List<Option> getOptions(@CheckForNull Field field) throws SysException {
    	if ( !field.useSelectInput || field.getForm().id != form.id
    	  || field.type.rawIsFile() || field.type.rawIsLongText() ) {
    		return Collections.emptyList();
    	}
    	if ( options == null ) {
        	options = new HashMap<String, List<Option>>();
            PluginImpl.getInstance().query(
            	"SELECT " + Utils.stringJoin( ",", PluginImpl.sqlQuote(
            		"FIELD", "VALUE", "HIDDEN"
                ) )  + String.format(
                	" FROM %1$s WHERE(%2$s='%3$s') ORDER BY `ORDER`",
                	PluginImpl.sqlQuote2One("OPTIONS"),
                	PluginImpl.sqlQuote2One("FORM"),
                	form.name,
                	PluginImpl.sqlQuote2One("VALUE")
                ),
                new SQLTransaction() { @Override public void run(Connection conn, Object... o) throws SysException {
         		  	try {
         		  		ResultSet rs = (ResultSet)o[0];
                        while ( rs.next() ) {
                        	String field = rs.getString(1);
                        	String value = rs.getString(2);
                        	boolean hidden = rs.getBoolean(3);
                        	if ( !options.containsKey(field) ) {
                        		options.put(field, new ArrayList<Option>());
                        	}
                            options.get(field).add(new Option(field, value, hidden));
                        }
                        for ( String key : options.keySet() ) {
                        	options.put(key, Collections.unmodifiableList(options.get(key)));
                        }
     		        }
     		        catch ( SQLException e ) {
     		            throw new SysException(
     		            	Messages.SCMFailedToInitializeField(),
     		            	e
     		            );
     		        }
            } } );
            options = Collections.unmodifiableMap(options);
    	}
    	if ( options.containsKey(field.name) ) {
    		return options.get(field.name);
    	}
        return Collections.emptyList();
    }
    
    public ExtendedField parseExtended(String fstr, boolean missingAsNull)
    		throws SysException {
		int dotIdx = -1;
		Fields fs = this;
    	String field = Utils.nullAsEmpty(fstr);
		List<Field> fp = new ArrayList<Field>();
		while ( (dotIdx = field.indexOf('.')) > 0  ) {
			Field p;
			if ( (p = fs.get(field.substring(0, dotIdx), true)) != null ) {
				if ( p.referred != null  ) {
					fp.add(0,p);
    				fs = p.referred.getFields();
				} else {
					break;
				}
			} else {
				break;
			}
			field = field.substring(dotIdx + 1);
		}
		Field f;
		if ( (f = fs.get(field, true)) != null ) {
    		ExtendedField df = new ExtendedField(f);
    		for ( Field p : fp ) {
    			df = new ExtendedField(p, df);
    		}
			return df;
		}
		if ( !missingAsNull ) {
    		throw new SysException( String.format(
    			Messages.SCMErrorFieldNotDefined(),
    			form.display(), fstr
    		) );
		}
		return null;
    }
    
    public ExtendedField parseExtended(String fstr)
    		throws SysException {
    	return parseExtended(fstr, false);
    }
    
    public class Option {
    	public final String value;
    	public final String display;
    	public boolean hidden;

    	public Option(String value, String display) {
    		this(value, display, true);
    	}
    	
    	public Option(String value, String display, boolean hidden) {
    		this.value = value;
    		this.hidden = hidden;
    		this.display = display;
    	}
    }
    
    class Field {
        public final long id;
        public final String name;
        public final TYPE type;
        public final Form referred;
        public String display = null;
        public String helpText = null;
        public String description = null;
        public String hookDefault = null;
        public String hookChecker = null;
        public String hookChanged = null;
        public boolean hidden = false;
        public boolean readonly = false;
        public boolean required = false;
        public boolean showTitle = true;
        public boolean useSelectInput = false;
        public boolean displayBlocked = false;
        private final List<ExtendedField> referredColumns;
                
        private Field (long id, @CheckForNull String name, @CheckForNull TYPE type, Form referred, String[] columns) throws SysException {
        	this.id = id;
        	this.name = name.trim().toUpperCase();
        	this.type = type;
        	this.referred = referred;
        	if ( type.rawIsReference() && referred == null ) {
        		throw new SysException( String.format(
        			Messages.SCMErrorReferredFormMissing(),
        			getForm().display(),
        			this.name
        		) );
        	}
        	List<ExtendedField> referredColumns = null;
        	if ( type.rawIsReference() ) {
        		List<String> valid = new ArrayList<String>();
        		if ( columns != null ) {
        			for ( String column : columns ) {
        				if ( (column = Utils.trimOrEmpty(column)).isEmpty() ) {
        					continue;
        				}
        				valid.add(column.toUpperCase());
        			}
        		}
                if ( valid.size() == 0 ) {
                    if ( referred.getUniques().containsKey("unique") ) {
                    	valid = referred.getUniques().get("unique");
                    } else if ( referred.getUniques().size() > 0 ) {
                        for ( List<String> v : referred.getUniques().values() ) {
                        	valid = v;
                        	break;
                        }
                    }
                }
                referredColumns = new ArrayList<ExtendedField>();
                for ( String c : valid  ) {
                	ExtendedField field;
                	if ( (field = referred.getFields().parseExtended(c, true)) == null ) {
                		continue;
                	}
                	referredColumns.add(field);
                }
                if ( referredColumns.size() == 0 ) {
                	referredColumns.add( new ExtendedField(
                		SYSFIELD.ID.get(Fields.this)
                	) );
                	referredColumns.add( new ExtendedField(
                		SYSFIELD.LASTMODIFIER.get(Fields.this)
                	) );
                	referredColumns.add( new ExtendedField(
                		SYSFIELD.LASTMODIFIED.get(Fields.this)
                	) );
                }
                referredColumns = Collections.unmodifiableList(referredColumns);
        	}
        	this.referredColumns = referredColumns;
        }
        
        public String getDisplay() {
        	if ( (display = Utils.trimOrEmpty(display)).isEmpty() ) {
        		return name;
        	}
        	return display;
        }
        
        public boolean isId() {
            return Utils.trimOrEmpty(name).toUpperCase()
            			== SYSFIELD.ID.toString();
        }
        
        public Object getDefault() throws HookException  {
        	Object value = null;
            String script = Utils.trimOrEmpty(hookDefault);
            if ( !script.isEmpty() ) {
            	try {
            		Map<String, Object> vars = new HashMap<String, Object>();
            		vars.put("formField", this);
            		value = Utils.evalGroovy(script, vars);
            		type.checkObjectType(this, value);
            	} catch ( RuntimeException e ) {
            		throw new HookException(
            			HookException.HOOK.DEFAULT,
            			this,
            			HookException.TYPE.RUNTIME,
            			e
            		);
            	} catch ( TypeMissingMatchException e ) {
            		throw new HookException(
            			HookException.HOOK.DEFAULT,
            			this,
            			HookException.TYPE.OUTPUT,
            			e
            		);
            	}
            }
            return value;
        }
        
    	public String[] getReferredTables() {
    		List<String> refs = new ArrayList<String>();
    		if ( referred != null ) {
    			refs.add("FORM__" + referred.name);
    		}
    		if ( type.rawIsArray() ) {
    			refs.add(0, String.format(
    				"REFS__%1$s__%2$s",
    				form.name,
    				name
    			) );
    		}
    		return refs.toArray(new String[refs.size()]);
    	}

        public Form getForm() {
            return form;
        }
                
        public List<Option> getOptions() throws SysException {
            if ( type.rawIsReference() ) {
            	Records records = new Records (
        			form, null, referredColumns,
        			null, 1, Integer.MAX_VALUE
            	);
            	List<Option> options = new ArrayList<Option>();
            	for ( Map<String, String> record : records.records ) {
            		String id = record.get(SYSFIELD.ID.toString());
            		String[] vs = new String[records.displayFields.size()];
            		for ( int i = 0; i< records.displayFields.size(); i++ ) {
            			ExtendedField field = records.displayFields.get(i);
            			vs[i] = record.get(field.getName());
            		}
        			options.add(new Option(id, Utils.stringJoin(", ", vs)));
            	}
            	return Collections.unmodifiableList(options);
            } else {
            	return Fields.this.getOptions(this);
            }
        }
    }
}
