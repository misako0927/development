/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2016
 *******************************************************************************/

package org.oscm.test.ws;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.oscm.logging.Log4jLogger;
import org.oscm.logging.LoggerFactory;
import org.oscm.types.enumtypes.LogMessageIdentifier;

public class NamespaceHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Log4jLogger logger = LoggerFactory
            .getLogger(NamespaceHandler.class);

    @Override
    public boolean handleMessage(SOAPMessageContext context) {

        try {

            Boolean outbound = (Boolean) context
                    .get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

            if (outbound.booleanValue()) {

                SOAPMessage message = context.getMessage();
                SOAPMessage messageChanged = null;
                String soapString = null;
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    message.writeTo(baos);
                    soapString = baos.toString();
                    soapString = soapString.replace("S:", "s:");
                    soapString = soapString.replace(":S", ":s");
                } catch (Exception e) {
                    throw new SOAPException(e);
                }

                try (ByteArrayInputStream bais = new ByteArrayInputStream(
                        soapString.getBytes())) {
                    MessageFactory factory = MessageFactory.newInstance();
                    messageChanged = factory.createMessage(
                            message.getMimeHeaders(), bais);
                    context.setMessage(messageChanged);
                } catch (Exception e) {
                    throw new SOAPException(e);
                }

                return true;
            } else {

                return true;

            }
        } catch (SOAPException e) {
            logger.logError(Log4jLogger.SYSTEM_LOG, e,
                    LogMessageIdentifier.ERROR_SOAP_CONVERSION);
        }
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    @Override
    public void close(MessageContext context) {
    }

    @Override
    public Set<QName> getHeaders() {
        return null;
    }

}
