package de.obfusco.secondhand.storage.model;

import javax.persistence.*;

@Entity(name = "customers")
@NamedNativeQueries({
        @NamedNativeQuery(
                name = "Customer.getZipCodeCounts",
                query = "select c.zip_code,count(*) as count from customers c group by c.zip_code",
                resultSetMapping = "ZipCodeCounts")
})
@SqlResultSetMappings({
        @SqlResultSetMapping(
                name = "ZipCodeCounts",
                classes = {
                        @ConstructorResult(
                                targetClass = ZipCodeCount.class,
                                columns = {
                                        @ColumnResult(name = "zip_code"),
                                        @ColumnResult(name = "count")
                                }
                        )
                }
        )
})
public class Customer extends AbstractEntity {

    @Column(name = "zip_code")
    private Integer zipCode;

    public Integer getZipCode() {
        return zipCode;
    }

    public void setZipCode(Integer zipCode) {
        this.zipCode = zipCode;
    }

    public static Customer create(Integer zipCode) {
        Customer customer = new Customer();
        customer.setZipCode(zipCode);
        return customer;
    }
}
