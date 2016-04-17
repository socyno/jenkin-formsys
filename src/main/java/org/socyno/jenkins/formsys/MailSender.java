package org.socyno.jenkins.formsys;


import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.socyno.jenkins.formsys.Messages;

import javax.annotation.CheckForNull;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.AddressException;

import java.io.UnsupportedEncodingException;

import hudson.Functions;
import hudson.tasks.Mailer;
import jenkins.model.JenkinsLocationConfiguration;

public class MailSender {
	private String body = null;
	private String subject = null;
	private boolean asHtml = false;
	private InternetAddress from = null;
	private final List<InternetAddress> addressTo
		= new ArrayList<InternetAddress>();
	private final List<InternetAddress> addressCc
	 	= new ArrayList<InternetAddress>();
	private final List<InternetAddress> addressBcc
 	= new ArrayList<InternetAddress>();
	
	private static final Logger logger 
			= Logger.getLogger(MailSender.class.getName());
	
	public static MimeMessage createMessage() {
		return new MimeMessage(Mailer.descriptor().createSession());
	}
	
	public static InternetAddress getFromAddress() throws AddressException {
		return StringToAddress(
			JenkinsLocationConfiguration.get().getAdminAddress()
		);
	}
	
	public static InternetAddress StringToAddress(String str) throws AddressException {
		str = Utils.trimOrEmpty(str);
		if ( str.isEmpty() || !str.contains("@") ) {
			throw new AddressException();
		}
		try {
			return Mailer.StringToAddress(str, Mailer.descriptor().getCharset());
		} catch (UnsupportedEncodingException e) {
			return StringToAddress(str, "UTF-8");
		}
	}
	
	public static InternetAddress StringToAddress(String str, String charset)
			throws AddressException {
		str = Utils.trimOrEmpty(str);
		if ( str.isEmpty() || !str.contains("@") ) {
			throw new AddressException();
		}
		try {
			return Mailer.StringToAddress(str, charset);
		} catch (UnsupportedEncodingException e) {
			return StringToAddress(str, "UTF-8");
		}
	}
	
	public static InternetAddress[] ObjectToAdddresses(Object o) {
		List<InternetAddress> addresses
			= new ArrayList<InternetAddress>();
		if (o instanceof String) {
			String addr = Utils.trimOrEmpty((String)o);
			if ( !addr.isEmpty() ) {
				try {
					addresses.add(StringToAddress(addr));
				} catch (AddressException e) {
					Users.Item user;
					if ( (user = Users.get(addr)) != null ) {
						InternetAddress[] _addr;
						if ( (_addr = ObjectToAdddresses(user)).length > 0 ) {
							addresses.add(_addr[0]);
						}
					} else {
						logger.warning( String.format(
							Messages.SCMErrorIvalidMailAddress(),
							addr
						) );
					}
				}
			}
		}
		else if ( o instanceof Users.Item ) {
			String addr = ((Users.Item)o).getMail();
			if ( !addr.isEmpty() ) {
				try {
					addresses.add(StringToAddress(addr));
				} catch( AddressException e ) {
					logger.warning( String.format(
						Messages.SCMErrorIvalidMailAddress(),
						addr
					) );
				}
			} else {
				logger.warning( String.format(
					Messages.SCMErrorUserNoMailAddress(),
					((Users.Item)o).getId()
				) );
			}
		} else if ( o instanceof Users ) {
			Users.Item _user;
			for ( int i = 0; i < ((Users)o).size(); i++ ) {
				if ( (_user = ((Users)o).get(i)) != null ) {
					InternetAddress[] _addr;
					if ( (_addr = ObjectToAdddresses(_user)).length > 0 ) {
						addresses.add(_addr[0]);
					}
				}
			}
		} else if ( o instanceof Object[] ) {
			Object _o;
			for ( int i = 0; i < ((Object[])o).length; i++ ) {
				if ( (_o = ((Object[])o)[i]) != null ) {
					InternetAddress[] _addr;
					if ( (_addr = ObjectToAdddresses(_o)).length > 0 ) {
						addresses.add(_addr[0]);
					}
				}
			}
		}
		return addresses.toArray(new InternetAddress[addresses.size()]);
	}
	
	public void setFrom(@CheckForNull InternetAddress _from ) {
		from = _from;
	}
	public void setBody(String _body) {
		body = _body;
	}

	public void setSubject(String _subject) {
		subject = _subject;
	}
	
	public void setRecipientsTO(Object address) {
		addressTo.clear();
		for( InternetAddress o : ObjectToAdddresses(address)) {
			addressTo.add(o);
		}
	}

	public void setRecipientsCC(Object address) {
		addressCc.clear();
		for( InternetAddress o : ObjectToAdddresses(address)) {
			addressCc.add(o);
		}
	}
	
	public void setRecipientsBCC(Object address) {
		addressBcc.clear();
		for( InternetAddress o : ObjectToAdddresses(address)) {
			addressBcc.add(o);
		}
	}
	
	public void setContentAsHTML(boolean _html) {
		asHtml = _html;
	}
	
	public void send() throws SysException {
		if ( from == null ) { 
			try {
				from = MailSender.getFromAddress();
	        } catch( AddressException e ) {
	        	return;
	        }
		}
		MimeMessage msg = null;
		try {
			msg = createMessage();
	      	msg.setFrom(from);
	        msg.setSentDate(new Date());
	        msg.setContent(
	        	Utils.nullAsEmpty(body),
	        	asHtml ? "text/html" : "text/plain"
	        );
	        msg.setSubject(Utils.nullAsEmpty(subject));
	        if ( addressTo.size() > 0 ) {
	       	 	msg.addRecipients (
	       	 		Message.RecipientType.TO,
	       	 		addressTo.toArray(new InternetAddress[addressTo.size()]
	       	 	) );
	        }
	        if ( addressCc.size() > 0 ) {
	       	 	msg.addRecipients (
	       	 		Message.RecipientType.CC,
	       	 		addressCc.toArray(new InternetAddress[addressCc.size()]
	       	 	) );
	        }
	        if ( addressBcc.size() > 0 ) {
	       	 	msg.addRecipients (
	       	 		Message.RecipientType.BCC,
	       	 		addressBcc.toArray(new InternetAddress[addressBcc.size()]
	       	 	) );
	        }
	        Transport.send(msg);
		} catch( MessagingException  e) {
			logger.warning(Functions.printThrowable(e));
			throw new SysException(Messages.SCMErrorMailException(), e);
		}
	}
}
