/*******************************************************************************
 *
 *  Copyright FUJITSU LIMITED 2016
 *
 *  Creation Date: Dec 2, 2013
 *
 *******************************************************************************/

package org.oscm.app.openstack;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.oscm.app.openstack.exceptions.HeatException;
import org.oscm.app.v1_0.exceptions.APPlatformException;

public class KeystoneClientTest {
    private final MockURLStreamHandler streamHandler = new MockURLStreamHandler();

    @Before
    public void setUp() throws Exception {
        OpenStackConnection.setURLStreamHandler(streamHandler);
        HeatProcessor.setURLStreamHandler(streamHandler);
    }

    @Test(expected = HeatException.class)
    public void authenticate_malFormedURL() throws HeatException,
            APPlatformException {
        // given

        // when
        new KeystoneClient(new OpenStackConnection("xyz")).authenticateV2("user",
                "password", "tenantName");
    }

    @Test
    public void authenticate() throws HeatException, APPlatformException {
        // given

        // when
        new KeystoneClient(new OpenStackConnection("http://xyz.de"))
                .authenticateV2("user", "password", "tenantName");
    }

    @Test
    public void authenticate_http_666() throws HeatException,
            APPlatformException {
        // given
        int status = 666;
        streamHandler.put("/tokens", new MockHttpURLConnection(status, ""));

        try {
            // when
            new KeystoneClient(new OpenStackConnection("http://xyz.de"))
                    .authenticateV2("user", "password", "tenantName");
            assertTrue("Test must fail at this point", false);
        } catch (RuntimeException ex) {
            // then
            assertTrue("Wrong exception message!",
                    ex.getMessage().indexOf("" + status) > -1);
        }
    }

    @Test(expected = RuntimeException.class)
    public void authenticate_noJSONResponse() throws HeatException,
            APPlatformException {
        // given
        streamHandler.put("/tokens", new MockHttpURLConnection(200, ""));

        // when
        new KeystoneClient(new OpenStackConnection("http://xyz.de"))
                .authenticateV2("user", "password", "tenantName");
    }

    @Test(expected = APPlatformException.class)
    public void authenticate_noHeatEndpoint() throws HeatException,
            APPlatformException {
        // given
        streamHandler.put("/tokens", new MockHttpURLConnection(200,
                MockURLStreamHandler.respTokens(false, true)));

        // when
        new KeystoneClient(new OpenStackConnection("http://xyz.de"))
                .authenticateV2("user", "password", "tenantName");
    }

    @Test(expected = APPlatformException.class)
    public void authenticate_noNovaEndpoint() throws HeatException,
            APPlatformException {
        // given
        streamHandler.put("/tokens", new MockHttpURLConnection(200,
                MockURLStreamHandler.respTokens(true, false)));

        // when
        new KeystoneClient(new OpenStackConnection("http://xyz.de"))
                .authenticateV2("user", "password", "tenantName");
    }

    @Test
    public void authenticate_v3() throws HeatException, APPlatformException {
        // given

        // when
        new KeystoneClient(new OpenStackConnection("https://xyz.de/v3/auth"))
                .authenticateV3("user", "password", "domainName", "tenantId");
    }

    @Test
    public void authenticate_v3_http_666() throws HeatException,
            APPlatformException {
        // given
        int status = 666;
        streamHandler.put("/v3/auth/tokens", new MockHttpsURLConnection(status, ""));

        try {
            // when
            new KeystoneClient(new OpenStackConnection("https://xyz.de/v3/auth"))
                    .authenticateV3("user", "password", "domainName", "tenantId");
            assertTrue("Test must fail at this point", false);
        } catch (RuntimeException ex) {
            // then
            assertTrue("Wrong exception message!",
                    ex.getMessage().indexOf("" + status) > -1);
        }
    }

    @Test(expected = HeatException.class)
    public void authenticate_v3_malFormedURL() throws HeatException,
            APPlatformException {
        // given

        // when
        new KeystoneClient(new OpenStackConnection("xyz"))
                .authenticateV3("user", "password", "domainName", "tenantId");
    }

    @Test(expected = RuntimeException.class)
    public void authenticate_v3_noJSONResponse() throws HeatException,
            APPlatformException {
        // given
        streamHandler.put("/v3/auth/tokens", new MockHttpsURLConnection(201, ""));

        // when
        new KeystoneClient(new OpenStackConnection("https://xyz.de/v3/auth"))
                .authenticateV3("user", "password", "domainName", "tenantId");

    }

    @Test(expected = APPlatformException.class)
    public void authenticate_v3_noHeatEndpoint() throws HeatException,
            APPlatformException {
        // given
        streamHandler.put("/v3/auth/tokens", new MockHttpsURLConnection(201,
                MockURLStreamHandler.respTokens_v3(false, true)));

        // when
        new KeystoneClient(new OpenStackConnection("https://xyz.de/v3/auth"))
                .authenticateV3("user", "password", "domainName", "tenantId");
    }

    @Test(expected = APPlatformException.class)
    public void authenticate_v3_noNovaEndpoint() throws HeatException,
            APPlatformException {
        // given
        streamHandler.put("/v3/auth/tokens", new MockHttpsURLConnection(201,
                MockURLStreamHandler.respTokens_v3(true, false)));

        // when
        new KeystoneClient(new OpenStackConnection("https://xyz.de/v3/auth"))
                .authenticateV3("user", "password", "domainName", "tenantId");
    }

}
