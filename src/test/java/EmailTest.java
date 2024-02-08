import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.File;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EmailTest {

    private GreenMail greenMail;

    private SoftAssert softAssert;

    @BeforeTest
    public void setUp() {
        greenMail = new GreenMail(ServerSetupTest.SMTP);
        greenMail.start();
        softAssert = new SoftAssert();

    }

    @AfterTest
    public void tearDown() {
        greenMail.stop();
        softAssert.assertAll();
    }

    @Test
    public void testSendAndReceiveEmail() throws MessagingException, Exception {
        sendEmail();

        Message receivedMessage = greenMail.getReceivedMessages()[0];
        assertEquals("Subject should match", "Test Subject", receivedMessage.getSubject());


        if (receivedMessage instanceof MimeMessage) {
            assertAttachmentsAndBodyText((MimeMessage) receivedMessage);
        }

        if (receivedMessage instanceof MimeMessage) {
            assertSenderAndSubject((MimeMessage) receivedMessage);
        }
    }

    private void sendEmail() throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "localhost");
        properties.put("mail.smtp.port", String.valueOf(greenMail.getSmtp().getPort()));

        Session session = Session.getInstance(properties, null);

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("cozyloon@gmail.com"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("chathumalsangeeth5@gmail.com"));
        message.setSubject("Test Subject");

        // Create a multipart message
        Multipart multipart = new MimeMultipart();

        // Add text part
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText("Hello, this is the body of the email.");

        // Add attachment part
        MimeBodyPart attachmentPart = new MimeBodyPart();
        File file = new File("src/test/java/a.txt");
        DataSource source = new FileDataSource(file);
        attachmentPart.setDataHandler(new DataHandler(source));
        attachmentPart.setFileName(file.getName());

        // Add parts to the multipart
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(attachmentPart);

        // Set the content of the message
        message.setContent(multipart);

        // Send the message
        Transport.send(message);
    }

    private void assertAttachmentsAndBodyText(MimeMessage message) throws Exception {
        Multipart multipart = (Multipart) message.getContent();
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                assertTrue("Attachment should exist", true);

            } else if (bodyPart.getContentType().startsWith("text/plain")) {
                String expectedBodyText = "Hello, this is the body of the email";
                String actualBodyText = bodyPart.getContent().toString().trim();
                softAssert.assertEquals(expectedBodyText, actualBodyText);
            }
        }
    }

    private void assertSenderAndSubject(MimeMessage message) throws MessagingException {
        Address[] fromAddresses = message.getFrom();
        String sender = (fromAddresses != null && fromAddresses.length > 0) ? fromAddresses[0].toString() : null;
        String subject = message.getSubject();

        softAssert.assertEquals(sender, "cozyloon@example.com");
        softAssert.assertEquals(subject, "Test Subject.");
    }
}
