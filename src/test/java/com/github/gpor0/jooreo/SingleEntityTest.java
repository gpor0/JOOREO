package com.github.gpor0.jooreo;

import com.github.gpor0.jooreo.test.support.JooreoTest;
import com.github.gpor0.jooreo.test.support.records.CustomerRecord;
import com.github.gpor0.jooreo.test.support.repositories.CustomerRepository;
import org.jboss.weld.junit5.EnableWeld;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

/**
 * Author: gpor0
 */
@EnableWeld
public class SingleEntityTest extends JooreoTest {

    @Inject
    CustomerRepository customerRepository;


    @Test
    public void shouldReturnTotalCount() {
        CustomerRecord customer = new CustomerRecord();
        customer.setFirstName("Ashley");
        customer.setLastName("Sinclair");
        customerRepository.create(customer);

        CustomerRecord customer2 = new CustomerRecord();
        customer2.setFirstName("Marsha");
        customer2.setLastName("May");
        customerRepository.create(customer2);

        Queried<CustomerRecord> all = customerRepository.getAll();
        assert all.getTotalCount() == 2;
    }

    @Test
    public void shouldUpdateField() {
        CustomerRecord customer = new CustomerRecord();
        customer.setFirstName("Lauren");
        customer.setLastName("May");
        customerRepository.create(customer);

        Queried<CustomerRecord> all = customerRepository.getAll();
        assert all.getTotalCount() == 1;

        CustomerRecord createdCustomer = customerRepository.getById(customer.getId());
        createdCustomer.setFirstName("Lauryn");
        customerRepository.update(createdCustomer);

        CustomerRecord updatedCustomer = customerRepository.getById(customer.getId());

        assert "Lauryn".equals(updatedCustomer.getFirstName());
    }

}
