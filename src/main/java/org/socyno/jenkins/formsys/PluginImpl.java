package org.socyno.jenkins.formsys;

import hudson.Plugin;
import hudson.Functions;
import hudson.model.Hudson;
import jenkins.model.Jenkins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import org.kohsuke.stapler.Stapler;

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import javax.annotation.CheckForNull;

import org.socyno.jenkins.formsys.Messages;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class PluginImpl extends Plugin {
    private ComboPooledDataSource dataSource = null;
    private static final String DATA_DRIVER = "mysql";
    private static final String DATA_DBNAME = "jenkins_scm";
    public static final String DATA_ATTACHES = "99bill/attaches";
    private static final Logger logger = Logger.getAnonymousLogger();
        
    @Override
    public void start() throws Exception {
        super.start();
        logger.info("Creating connection pool to database ...");
        dataSource = new ComboPooledDataSource();
        dataSource.setMinPoolSize(5);
        dataSource.setMaxPoolSize(10);
        dataSource.setAcquireIncrement(5);
        dataSource.setPreferredTestQuery("SELECT 1");
        dataSource.setIdleConnectionTestPeriod(600);
        dataSource.setDriverClass("com.mysql.jdbc.Driver"); 
        dataSource.setJdbcUrl(
       		"jdbc:mysql://192.168.138.100/jenkins_scm"
        	+ "?useUnicode=true&characterEncoding=UTF8&autoReconnect=true"
        );
        init();
        logger.info("Started, DB Driver = " + DATA_DRIVER);
    }
    
    public static void warning(Object mesg) {
		Throwable e = new Throwable();
        StackTraceElement[] s = e.getStackTrace();
        if ( mesg == null ) {
        	mesg = "";
        }
        mesg = String.format(
        	"%1$s/%2$s/%3$d:%4$s",
        	s[1].getClassName(),
        	s[1].getMethodName(),
        	s[1].getLineNumber(),
        	mesg instanceof Throwable ?
				Functions.printThrowable((Throwable)mesg)
				: mesg.toString()
		);
		logger.warning((String)mesg);
    }
    
    public static void fine(Object mesg) {
    	if ( mesg != null  ) {
    		logger.fine(mesg.toString());
    	}
    }

    public static void info(Object mesg) {
    	if ( mesg != null  ) {
    		logger.info(mesg.toString());
    	}
    }
    
    private synchronized void init() throws SQLException, SysException {
    	Connection conn = getConnection();
    	Statement sth = conn.createStatement();
        logger.info("Initilizing 99bill scm database ...");
         sth.execute("USE " + DATA_DBNAME);
         sth.execute("CREATE TABLE IF NOT EXISTS `CONFIGURATIONS` ("
             + "`NAME` VARCHAR(64) NOT NULL PRIMARY KEY,"
             + "`VALUE` VARCHAR(255)"
             + ")");
         int revision = 0;
         ResultSet rs = sth.executeQuery("SELECT `VALUE` FROM `CONFIGURATIONS` "
             + "WHERE `NAME`='schema_revision'");
         if ( rs.next() ) {
             revision = rs.getInt(1);
         }
         else {
             sth.execute("INSERT INTO `CONFIGURATIONS` (`NAME`, `VALUE`)"
                 + " VALUES('schema_revision', '0')");
         }
         int newrevison = getSchemaRevision();
         logger.info("Database schema revision is " + revision);
         if ( newrevison > revision && revision >= 0 ) {
        	 for ( int i = revision + 1; i <= newrevison; i++ ) {
        		 conn.setAutoCommit(false);
        		 try {
        			 sqlSourceRun(sth, String.format("/sql/rev-%1$d.sql", i));
        			 sth.execute("UPDATE `CONFIGURATIONS` SET `VALUE`='" + i
     					 	+ "' WHERE `NAME`='schema_revision'");
        		 } catch ( SQLException e ) {
        			 conn.rollback();
        		 } finally {
        			 conn.setAutoCommit(true);
        		 }
        	 }
         }
    }
    

    /**
     * Returns the singleton instance.
     *
     * @return the one.
     */
    public static PluginImpl getInstance() {
        return Hudson.getInstance().getPlugin(PluginImpl.class);
    }
        
    public static String getUrl() {
    	return Home.getUrl();
    }
    
    public String getDBDriverName() {
    	return DATA_DRIVER;
    }
    
    public static String sqlCoalesce(@CheckForNull String... strs) {
    	return String.format("COALESCE(%1$s)",Utils.stringJoin(", ", (Object[])strs));
    }
    
    public static String sqlEscape(@CheckForNull String str) {
		if ( getInstance().getDBDriverName().contains("mysql") ) {
			str = str.replaceAll("\\\\", "\\\\\\\\")
					  .replaceAll("_", "\\\\_")
					  .replaceAll("%", "\\\\%")
					  .replaceAll("'", "\\\\'");
		}
		return str;
    }
    
    public static String sqlQuote2One(@CheckForNull String... strs) {
    	String[] quoteChar = sqlQuoteChars();
    	return String.format(
			"%1$s%2$s%3$s",
			quoteChar[0],
			Utils.stringJoin( String.format(
				"%1$s.%2$s",
				quoteChar[1],
				quoteChar[0]
			), strs ),
			quoteChar[1]
		);
    }
    
    public static String[] sqlQuote(List<String[]> columns) {
    	String[] quoteChar = sqlQuoteChars();
    	List<String> quoted = new ArrayList<String>();
    	if ( columns != null && columns.size() > 0 ) {
	    	for ( String[] sa : columns ) {
	    		quoted.add( String.format(
	    			"%1$s%2$s%3$s",
	    			quoteChar[0],
	    			Utils.stringJoin( String.format(
	    				"%1$s.%2$s",
	    				quoteChar[1],
	    				quoteChar[0]
	    			), sa == null ? new String[0] : sa ),
	    			quoteChar[1]
	    		) );
	    	}
    	}
    	return quoted.toArray(new String[quoted.size()]);
    }

    public static String[] sqlQuote(String... columns) {
    	String[] quoteChar = sqlQuoteChars();
    	List<String> quoted = new ArrayList<String>();
    	if ( columns != null && columns.length > 0 ) {
	    	for ( String c : columns ) {
	    		quoted.add( String.format(
	    			"%1$s%2$s%3$s",
	    			quoteChar[0],
	    			Utils.nullAsEmpty(c),
	    			quoteChar[1]
	    		) );
	    	}
    	}
    	return quoted.toArray(new String[quoted.size()]);
    }
    
    public static String[] sqlQuote(String[]... columns) {
    	return sqlQuote(Arrays.asList(columns));
    }
     
    public static String sqlQuoted2As(@CheckForNull String quotedColumn) {
    	String[] quoteChar = sqlQuoteChars();
    	return quotedColumn.replaceAll( Utils.regexpEscape(
    			quoteChar[0] + "." + quoteChar[1]
    	), "_" );
    }
    
    public static String[] sqlQuoteChars() {
    	String[] quoteChar = new String[]{"", ""};
    	String driver = getInstance().getDBDriverName();
    	if ( driver.contains("mysql") || driver.contains("h2") ) {
    		quoteChar = new String[]{"`", "`"};
    	}
    	return quoteChar;
    }
    
    public static String sqlEscapeEquals(@CheckForNull String str) {
		if ( getInstance().getDBDriverName().contains("mysql") ) {
			str = str.replaceAll("\\\\", "\\\\\\\\")
					  .replaceAll("'", "\\\\'");
		}
		return str;
    }

    /**
     * Run SQL commands from given source
     * @param source : SQL source name
     * @throws SysException
     * @throws SQLException 
     */
    public static void sqlSourceRun(@CheckForNull Statement statement, @CheckForNull String source)
    		throws SysException, SQLException {
    	List<String> commands;
    	commands = sqlSourceParse(source);
		for (String sql : commands) {
			try {
				statement.execute(sql);
			} catch (SQLException e) {
				throw new SQLException( String.format(
					Messages.SCMErrorSQLSourceRun(),
					source, sql
				), e );
			}
		}
	}

    /**
     * Parse SQL commands from given source
     * @param source : SQL source name
     * @return a list of SQL commands
     * @throws SysException
     */
    private static List<String> sqlSourceParse(@CheckForNull String source)
    		throws SysException {
    	InputStream stream = null;
    	BufferedReader reader = null;
    	String path = PluginImpl.class.getPackage()
    				.getName().replaceAll("\\.", "/");
    	while ( source.startsWith("/") ) {
    		source = source.substring(1);
    	}
    	try {
    		stream = PluginImpl.class.getResourceAsStream(path + "/" + source);
    		reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
    		String line, delimiter = ";";
    		List<String> commands = new ArrayList<String>();
    		StringBuilder buffer = new StringBuilder();
    		while ( (line = reader.readLine()) != null ) {
	    		if (line.startsWith("--")) {
	    			continue;
	    		}
	    		if ( buffer.length() == 0 && line.toLowerCase().startsWith("delimiter ") ) {
	    			delimiter = line.substring("delimiter ".length()).trim();
	    			continue;
	    		}
	
	    		if (buffer.length() > 0) {
	    			buffer.append('\n');
	    		}
	    		buffer.append(line);
	    		
	    		boolean isEnd = false;
	    		if ( ";".equals(delimiter) ) {
	    			isEnd = buffer.charAt(buffer.length() - 1) == ';';
	    		} else if (line.equals(delimiter)) {
	    			buffer.setLength(buffer.length() - delimiter.length());
	    			isEnd = true;
	    		}
	    		if ( isEnd ) {
	    			String cmd = buffer.toString();
	    			commands.add(cmd.trim());
	    		   	buffer = new StringBuilder();
	    		}
	    	}
	    	if (buffer.length() > 0) {
	    		commands.add(buffer.toString().trim());
	    	}
	    	return commands;
    	} catch ( Exception e ) {
	    	throw new SysException( String.format(
				Messages.SCMErrorSQLSourceParse(),source
			), e );
	    } finally {
	    	try {
	    		if ( stream != null ) {
	    			stream.close();
	    		}
	    	} catch ( IOException e ) {
	    		
	    	}
    	}
	}
    
    
    /**
     * Get schema revision number
     * 
     */
    public int getSchemaRevision() {
        return 8;
    }    
    
    /**
     * Execute a select statement
     *
    */
    public void query(String sql, SQLTransaction tran)
    		throws SysException {
    	query(sql, 0, tran);
    }
    public void query(String sql, int limit, SQLTransaction tran)
    		throws SysException {
    	query(sql, limit, 0, tran);
    }
    public void query(String sql, int limit, int offset,  SQLTransaction tran)
    		throws SysException {
        PluginImpl.info(sql = Utils.trimOrEmpty(sql));
        if ( !sql.toLowerCase().startsWith("select ") ) {
            throw new SysException(
            	Messages.SCMErrorSQLIvalidSelectStatement()
            );
        }
        ResultSet rs = null ;
        Statement sth = null;
        Connection conn = null;
        try {
            conn = getConnection();
        	sth = conn.createStatement(
        		ResultSet.TYPE_FORWARD_ONLY,
        		ResultSet.CONCUR_READ_ONLY
        	);

            if ( limit > 0 ) {
            	sth.setMaxRows(limit);
            }
        	sth.setFetchSize(Integer.MIN_VALUE);
        	rs = sth.executeQuery(sql);
        	if ( tran != null ) {
                if ( offset > 0 ) {
                	rs.absolute(offset);
                }
    	    	tran.run(conn, rs);
        	}
        } catch(SQLException e) {
            throw new SysException(
            	Messages.DBFailedToExecuteStatement(),
            	e
            );
        } finally {
        	try {
	        	if ( rs != null && !rs.isClosed() ) {
	        		rs.close();
	        	}
	        	if ( sth != null && !sth.isClosed() ) {
	        		sth.close();
	        	}
	        	if ( conn != null && !conn.isClosed() ) {
	        		conn.close();
	        	}
        	} catch( SQLException e) {
        		logger.warning(Functions.printThrowable(e));
        	}
        }
    }
    
    public void SQLQueryWithPager(String sql, int page, int size, SQLTransaction tran)
    		throws SysException {
    	if ( getInstance().getDBDriverName().contains("mysql") ) {
    		sql += " LIMIT " + ((page - 1) * size) + "," + size;
    		query(sql, tran);
    	} else {
    		int offset = (page - 1) * size;
    		query(sql, size + offset, offset, tran);
    	}
    }
    
    public synchronized void insert(String sql, SQLTransaction tran) throws SysException {
    	ResultSet result = null;
    	Connection connection = null;
    	PreparedStatement statement = null;
    	try {
    		sql = Utils.trimOrEmpty(sql);
            if ( !sql.toLowerCase().startsWith("insert ") ) {
                throw new SysException(
                	Messages.SCMErrorSQLIvalidInsertStatement()
                );
            }
    	    connection = getConnection();
    	    statement = connection.prepareStatement(
  	    		  sql, Statement.RETURN_GENERATED_KEYS);
    	    if ( tran != null ) {
    	    	try {
    	    		connection.setAutoCommit(false);
    	    		statement.executeUpdate();
    	    		result = statement.getGeneratedKeys();
    	    		tran.run(connection, result);
    	    		connection.commit();
    	    	} catch( SysException e) {
    	    		throw e;
    	    	}
    	    	finally {
    	    		if ( result != null ) {
    	    			connection.rollback();
    	    		}
    	    	}
    	    } else {
    	    	statement.executeUpdate();
    	    }
        } catch ( SQLException  e ) {
        	throw new SysException(
        		Messages.DBFailedToExecuteStatement(),
        		e
        	);
        } finally {
        	try {
	        	if ( result != null && !result.isClosed() ) {
	        		result.close();
	        	}
	        	if ( statement != null && !statement.isClosed() ) {
	        		statement.close();
	        	}
	        	if ( connection != null && !connection.isClosed() ) {
	        		connection.close();
	        	}
        	} catch( SQLException e) {
        		logger.warning(Functions.printThrowable(e));
        	}
        }
    }
    
    public synchronized void insert(String sql) throws SysException {
        insert(sql, null);
    }
    
    public static boolean isSystemAdministrator() {
    	return Jenkins.getInstance().hasPermission(Jenkins.ADMINISTER);
    }
    
    public synchronized int update(String sql, SQLTransaction tran) throws SysException {
    	int result = 0;
    	Connection connection = null;
    	PreparedStatement statement = null;
    	try {
    		sql = Utils.trimOrEmpty(sql);
            if ( !sql.toLowerCase().startsWith("update ")
              || !sql.toLowerCase().startsWith("delete ")
            ) {
                throw new SysException(
                	Messages.SCMErrorSQLIvalidUpdateStatement()
                );
            }
    	    connection = getConnection();
    	    statement = connection.prepareStatement(sql);
    	    if ( tran != null ) {
    	    	boolean executed = false;
    	    	try {
    	    		connection.setAutoCommit(false);
    	    		result = statement.executeUpdate();
    	    		executed = true; 
    	    		tran.run(connection, result);
    	    		connection.commit();
    	    	} finally {
    	    		if ( executed ) {
        	    		connection.rollback();
    	    		}
    	    		connection.setAutoCommit(true);
    	    	}
    	    } else {
    	    	result = statement.executeUpdate();
    	    }
        } catch ( SQLException  e ) {
        	throw new SysException(
        		Messages.DBFailedToExecuteStatement(),
        		e
        	);
        } finally {
        	try {
	        	if ( statement != null && !statement.isClosed() ) {
	        		statement.close();
	        	}
	        	if ( connection != null && !connection.isClosed() ) {
	        		connection.close();
	        	}
        	} catch( SQLException e) {
        		logger.warning(Functions.printThrowable(e));
        	}
        }
    	return result;
    }

    public synchronized int update(String sql) throws SysException {
        return update(sql, null);
    }

    
    private synchronized Connection getConnection() throws SysException { 
    	Connection conn = null;
    	try {
    		conn = dataSource.getConnection(); 
    		conn.setAutoCommit(true);
    	} catch (SQLException e) {
	        throw new SysException(
	        	Messages.SCMErrorCreateDBConnection(),
	        	e
	        );
    	}
        return conn;
    }

    /**
     * Calculates the path to the icon.
     * Copied from {@link jenkins.model.ModelObjectWithContextMenu.ContextMenu#add(hudson.model.Action)}
     *
     * @param base the suffix of the icon path
     * @return the full path of the icon including resources.
     */
    public static String getIconPath(String base) {
        if (base.startsWith("images/")) {
            return Stapler.getCurrentRequest().getContextPath() + Functions.getResourcePath() + '/' + base;
        } else {
            return Stapler.getCurrentRequest().getContextPath() + "" + '/' + base;
        }
    }
}

