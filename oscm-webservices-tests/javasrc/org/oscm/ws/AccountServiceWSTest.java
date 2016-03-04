/*******************************************************************************
 *                                                                              
 *  Copyright FUJITSU LIMITED 2016                                             
 *                                                                                                                                 
 *  Creation Date: Mar 5, 2012                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.ws;

import static org.junit.Assert.fail;

import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.oscm.intf.AccountService;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOOrganizationPaymentConfiguration;
import org.oscm.vo.VOPaymentType;
import org.oscm.vo.VOServicePaymentConfiguration;
import org.oscm.vo.VOUserDetails;
import org.oscm.ws.base.ServiceFactory;
import org.oscm.ws.base.WebserviceTestBase;
import org.oscm.ws.base.WebserviceTestSetup;

/**
 * @author stavreva
 */
public class AccountServiceWSTest {

    private WebserviceTestSetup setup;
    private static AccountService accountService_Supplier;
    private static AccountService accountService_Customer;
    private static VOUserDetails customerUser;
    private static VOOrganization customerOrg;

    @Before
    public void setUp() throws Exception {
        // clean the mails
        WebserviceTestBase.getMailReader().deleteMails();

        // add currencies
        WebserviceTestBase.getOperator().addCurrency("EUR");

        setup = new WebserviceTestSetup();

        // Create two suppliers
        setup.createSupplier("Supplier1");
        setup.createTechnicalService();

        // Retrieve AccountService of Supplier
        accountService_Supplier = ServiceFactory.getDefault()
                .getAccountService(setup.getSupplierUserKey(),
                        WebserviceTestBase.DEFAULT_PASSWORD);

        // Create customer
        customerOrg = setup.createCustomer("Customer");
        accountService_Customer = setup.getAccountServiceForCustomer();
        customerUser = setup.getCustomerUser();

        accountService_Customer = ServiceFactory.getDefault()
                .getAccountService(String.valueOf(customerUser.getKey()),
                        WebserviceTestBase.DEFAULT_PASSWORD);
    }

    @Test
    public void deregisterOrganization_Customer() throws Exception {

        Set<VOPaymentType> defaultPaymentTypes = accountService_Supplier
                .getDefaultPaymentConfiguration();
        Set<VOPaymentType> defaultServicePaymentTypes = accountService_Supplier
                .getDefaultServicePaymentConfiguration();
        List<VOOrganizationPaymentConfiguration> customerConfigList = accountService_Supplier
                .getCustomerPaymentConfiguration();
        List<VOServicePaymentConfiguration> serviceConfigList = accountService_Supplier
                .getServicePaymentConfiguration();

        // set payment type for a customer organization
        for (VOOrganizationPaymentConfiguration customerConfig : customerConfigList) {
            if (customerConfig.getOrganization().getName()
                    .contains(customerOrg.getName())) {
                customerConfig.getEnabledPaymentTypes().addAll(
                        defaultPaymentTypes);
                break;
            }
        }

        accountService_Supplier.savePaymentConfiguration(defaultPaymentTypes,
                customerConfigList, defaultServicePaymentTypes,
                serviceConfigList);

        // deregister fails because of existing payment type for the customer
        // organization
        try {
            accountService_Customer.deregisterOrganization();
            fail();
        } catch (Exception e) {
        }

        defaultPaymentTypes = accountService_Supplier
                .getDefaultPaymentConfiguration();
        defaultServicePaymentTypes = accountService_Supplier
                .getDefaultServicePaymentConfiguration();
        customerConfigList = accountService_Supplier
                .getCustomerPaymentConfiguration();
        serviceConfigList = accountService_Supplier
                .getServicePaymentConfiguration();

        // delete the payment types for the customer organization
        for (VOOrganizationPaymentConfiguration customerConfig : customerConfigList) {
            customerConfig.getEnabledPaymentTypes().clear();
        }

        accountService_Supplier.savePaymentConfiguration(defaultPaymentTypes,
                customerConfigList, defaultServicePaymentTypes,
                serviceConfigList);

        // deregister succeeds
        accountService_Customer.deregisterOrganization();

    }

    @Test
    public void deregisterOrganization_Supplier() throws Exception {

        // deregister supplier fails, because of the payment types which can be
        // set as default (invoice is always present)
        try {
            accountService_Supplier.deregisterOrganization();
            fail();
        } catch (Exception e) {
        }

    }

}
