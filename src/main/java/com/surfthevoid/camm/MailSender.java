package com.surfthevoid.camm;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.springframework.stereotype.Component;

@Component
public class MailSender {

	private String host = "";
	private int port = 0;
	private String username = "";
	private String password = "";

	public MailSender() {
		this.host = "smtp.live.com";
		this.port = 587;
		this.username = "charles.huber@hotmail.fr";
		this.password = "PhLoToNs_1123";
	}

	public void sendMail(String msg, byte[]... jpegImages) {

		Properties prop = new Properties();
		prop.put("mail.smtp.auth", true);
		prop.put("mail.smtp.starttls.enable", "true");
		prop.put("mail.smtp.host", host);
		prop.put("mail.smtp.port", port);
		prop.put("mail.smtp.ssl.trust", host);

		Session session = Session.getInstance(prop, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("charles.huber@hotmail.fr"));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("charles.huber@hotmail.fr"));
			message.setSubject("ALARME !!");

			MimeBodyPart mimeBodyPart = new MimeBodyPart();
			mimeBodyPart.setContent(msg, "text/html");
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(mimeBodyPart);
			
			for(int i=0; i < jpegImages.length; i++){
				byte[] jpg = jpegImages[i];
				DataSource dataSource = new ByteArrayDataSource(jpg, "application/jpg");
				MimeBodyPart imgBodyPart = new MimeBodyPart();
				imgBodyPart.setDataHandler(new DataHandler(dataSource));
				imgBodyPart.setFileName("img" + i + ".jpg");
				multipart.addBodyPart(imgBodyPart);
			}
			message.setContent(multipart);
			Transport.send(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
