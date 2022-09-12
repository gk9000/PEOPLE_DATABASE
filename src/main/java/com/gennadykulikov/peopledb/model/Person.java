package com.gennadykulikov.peopledb.model;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

public class Person implements Entity {
    private Long id;
    private String firstName;
    private String lastName;
    private ZonedDateTime dob;
    private BigDecimal salary;
    private String email;
    private Optional <Address> homeAddress = Optional.empty();
    private Optional <Address> businessAddress = Optional.empty();


    public Person(String firstName, String lastName, ZonedDateTime dob,BigDecimal salary) {
        this.firstName=firstName;
        this.lastName=lastName;
        this.dob=dob;
        this.salary=salary;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public ZonedDateTime getDob() {
        return dob;
    }

    public void setDob(ZonedDateTime dob) {
        this.dob = dob;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;

        Person person = (Person) o;

        if (id != null ? !id.equals(person.id) : person.id != null) return false;
        if (!firstName.equals(person.firstName)) return false;
        if (!lastName.equals(person.lastName)) return false;
        if (!salary.equals(person.salary)) return false;
        return dob.withZoneSameInstant(ZoneId.of("+0")).equals(person.dob.withZoneSameInstant(ZoneId.of("+0")));
    }

//    @Override
//    public int hashCode() {
//        int result = id != null ? id.hashCode() : 0;
//        result = 31 * result + firstName.hashCode();
//        result = 31 * result + lastName.hashCode();
//        result = 31 * result + dob.hashCode();
//        return result;
//    }

    //        return dob != null ? dob.withZoneSameInstant(ZoneId.of("+0")).equals(person.dob.withZoneSameInstant(ZoneId.of("+0"))) : person.dob == null;




    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dob=" + dob +
                ", salary=" + salary +
                ", home address=" + homeAddress +
                ", business address=" + businessAddress +
                '}';
    }

    public void setHomeAddress(Address homeAddress) {
        this.homeAddress = Optional.ofNullable(homeAddress);
    }
    public Optional <Address> getHomeAddress() {
        return homeAddress;
    }


    public void setBusinessAddress(Address businessAddress) {
        this.businessAddress = Optional.ofNullable(businessAddress);
    }
    public Optional <Address> getBusinessAddress() {
        return businessAddress;
    }
}
