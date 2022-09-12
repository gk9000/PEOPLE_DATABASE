package com.gennadykulikov.peopledb.repository;

import com.gennadykulikov.peopledb.model.Address;
import com.gennadykulikov.peopledb.model.Person;
import com.gennadykulikov.peopledb.model.Region;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class PeopleRepositoryTests {

    private Connection connection;
    private PeopleRepository repo;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection(("jdbc:h2:~/peopletest").replace("~", System.getProperty("user.home")));
        connection.setAutoCommit(false);
        repo = new PeopleRepository(connection);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if(connection != null){
            connection.close();
        }
    }

    @Test
    public void canSaveOnePerson() throws SQLException {
        Person sergei = new Person ("Sergei", "Skvortsov", ZonedDateTime.of(1990, 02, 18, 22, 40, 25,0, ZoneId.of("+3")),new BigDecimal("1111.11"));
        Person savedPerson = repo.save(sergei);
        assertThat(savedPerson.getId()).isGreaterThan(0);
    }

    @Test
    public void canSaveTwoPeople(){
        Person anna = new Person ("Anna", "Kholmogorova", ZonedDateTime.of(1990, 02, 18, 22, 40, 25,6, ZoneId.of("+3")),new BigDecimal("1111.11"));
        Person alex = new Person ("Alexandr", "Vasechkin", ZonedDateTime.of(1996, 07, 18, 22, 40, 25,6, ZoneId.of("+3")),new BigDecimal("1111.11"));
        Person savedPerson1 = repo.save(anna);
        Person savedPerson2 = repo.save(alex);
        assertThat(savedPerson1.getId()).isNotEqualTo(savedPerson2.getId());
    }

    @Test
    public void canSavePersonWithHomeAddress() throws SQLException {
        Person anna = new Person ("Anna", "Zvereva", ZonedDateTime.of(1990, 02, 18, 22, 40, 25,6, ZoneId.of("+3")),new BigDecimal("1111.11"));
        Address homeAddress = new Address(null, "123 Short Str.", "456 Long Str.", "Iron Big Town", "Fulton County", "CA", "642-643", Region.SOUTHWEST, "US");
        anna.setHomeAddress(homeAddress);
        Person savedPerson = repo.save(anna);
        assertThat(savedPerson.getHomeAddress().get().getId()).isGreaterThan(0);
        assertThat(savedPerson.getHomeAddress().get().getCity()).isEqualTo("Iron Big Town");
    }

    @Test
    public void canSavePersonWithBusinessAddress() throws SQLException {
        Person anna = new Person ("Sergei", "Petrov", ZonedDateTime.of(1990, 02, 18, 22, 40, 25,6, ZoneId.of("+3")),new BigDecimal("1111.11"));
        Address businessAddress = new Address(null, "999 Sun Av.", "777 Flower Str.", "Iron Small Town", "Fulton County", "CA", "642-643", Region.SOUTHWEST, "US");
        anna.setBusinessAddress(businessAddress);
        Person savedPerson = repo.save(anna);
        assertThat(savedPerson.getBusinessAddress().get().getId()).isGreaterThan(0);
        assertThat(savedPerson.getBusinessAddress().get().getCity()).isEqualTo("Iron Small Town");
    }

    @Test
    public void canFindPersonById(){
        Person savedPerson = repo.save(new Person("Test", "Test", ZonedDateTime.of(1990, 02, 18, 22, 10, 10, 100000, ZoneId.of("+2")), new BigDecimal("1111.11")));
        System.out.println(savedPerson);
        Person foundPerson = repo.findById(savedPerson.getId()).get();
        System.out.println(foundPerson);
        assertThat(savedPerson).isEqualTo(foundPerson);
    }

    @Test
    public void canFindPersonByIdWithHomeAddress() throws SQLException {
        Person elena = new Person ("Elena", "Maksimova", ZonedDateTime.of(1990, 02, 18, 22, 40, 25,6, ZoneId.of("+3")),new BigDecimal("1111.11"));
        Address address = new Address(null, "33 Big Str.", "44 Small Str.", "Iron Town", "Fulton County", "CA", "642-643", Region.SOUTHWEST, "US");
        elena.setHomeAddress(address);
        System.out.println(elena);
        Person savedPerson = repo.save(elena);
        System.out.println("saved "+savedPerson);
        Person foundPerson = repo.findById(savedPerson.getId()).get();
        System.out.println("found "+foundPerson);
        assertThat(foundPerson.getHomeAddress().get().getState()).isEqualTo("CA");
    }

    @Test
    public void canFindPersonByIdWithBusinessAddress() throws SQLException {
        Person elena = new Person ("Elena", "Maksimova", ZonedDateTime.of(1990, 02, 18, 22, 40, 25,6, ZoneId.of("+3")),new BigDecimal("1111.11"));
        Address address = new Address(null, "33 Big Str.", "44 Small Str.", "Iron Town", "Fulton County", "NY", "642-643", Region.SOUTHWEST, "US");
        elena.setBusinessAddress(address);
        System.out.println("created person " + elena);
        Person savedPerson = repo.save(elena);
        System.out.println("saved person " + savedPerson);
        Person foundPerson = repo.findById(savedPerson.getId()).get();
        assertThat(foundPerson.getBusinessAddress().get().getState()).isEqualTo("NY");
    }

    @Test
    public void canFindPersonByIdWithBothAddresses() throws SQLException {
        Person elena = new Person ("Elena", "Popova", ZonedDateTime.of(1990, 02, 18, 22, 40, 25,6, ZoneId.of("+3")),new BigDecimal("1111.11"));
        Address homeAddress = new Address(null, "33 Big Str.", "ap.5", "Iron Town", "Fulton County", "NY", "642-643", Region.SOUTHWEST, "US");
        Address businessAddress = new Address(null, "666 Satan Str.", "Office 44", "Iron Town", "Fulton County", "FL", "642-643", Region.SOUTHWEST, "US");
        elena.setHomeAddress(homeAddress);
        elena.setBusinessAddress(businessAddress);

        System.out.println("created person " + elena);
        Person savedPerson = repo.save(elena);
        System.out.println("saved person " + savedPerson);
        Person foundPerson = repo.findById(savedPerson.getId()).get();
        System.out.println("found person " + foundPerson);

        System.out.println("found person " + foundPerson);
        assertThat(foundPerson.getHomeAddress().get().getState()).isEqualTo("NY");
        assertThat(foundPerson.getBusinessAddress().get().getState()).isEqualTo("FL");
    }


    @Test
    public void canFindAll(){
        long startCount = repo.count();
        repo.save(new Person("Test1", "Test123", ZonedDateTime.of(1990, 02, 18, 22, 10, 10, 100000, ZoneId.of("+2")), new BigDecimal("1111.11")));
        repo.save(new Person("Test2", "Test123", ZonedDateTime.of(1990, 02, 18, 22, 10, 10, 100000, ZoneId.of("+2")), new BigDecimal("1111.11")));
        repo.save(new Person("Test3", "Test123", ZonedDateTime.of(1990, 02, 18, 22, 10, 10, 100000, ZoneId.of("+2")), new BigDecimal("1111.11")));
        repo.save(new Person("Test4", "Test123", ZonedDateTime.of(1990, 02, 18, 22, 10, 10, 100000, ZoneId.of("+2")), new BigDecimal("1111.11")));
        repo.save(new Person("Test5", "Test123", ZonedDateTime.of(1990, 02, 18, 22, 10, 10, 100000, ZoneId.of("+2")), new BigDecimal("1111.11")));
        repo.save(new Person("Test6", "Test123", ZonedDateTime.of(1990, 02, 18, 22, 10, 10, 100000, ZoneId.of("+2")), new BigDecimal("1111.11")));
        repo.save(new Person("Test7", "Test123", ZonedDateTime.of(1990, 02, 18, 22, 10, 10, 100000, ZoneId.of("+2")), new BigDecimal("1111.11")));
        repo.save(new Person("Test8", "Test123", ZonedDateTime.of(1990, 02, 18, 22, 10, 10, 100000, ZoneId.of("+2")), new BigDecimal("1111.11")));
        repo.save(new Person("Test9", "Test123", ZonedDateTime.of(1990, 02, 18, 22, 10, 10, 100000, ZoneId.of("+2")), new BigDecimal("1111.11")));
        repo.save(new Person("Test10", "Test123", ZonedDateTime.of(1990, 02, 18, 22, 10, 10, 100000, ZoneId.of("+2")), new BigDecimal("1111.11")));
        List<Person> people = repo.findAll();
        assertThat(people.size()).isEqualTo(startCount + 10);
    }


    @Test
    public void testPersonIdNotFound(){
        Person foundPerson = repo.findById(-1L).orElse(null);
        System.out.println(foundPerson);
        assertThat(foundPerson).isNull();
    }

    @Test
    public void canGetCount(){
        long startCount = repo.count();
        repo.save(new Person("Test", "Test", ZonedDateTime.of(1990, 02, 18, 22, 10, 10,100000, ZoneId.of("+2")),new BigDecimal("1111.11")));
        repo.save(new Person("Test1", "Test", ZonedDateTime.of(1990, 02, 18, 22, 10, 10, 100000, ZoneId.of("+2")),new BigDecimal("1111.11")));
        long endCount = repo.count();
        assertThat(endCount).isEqualTo(startCount+2);
    }

    @Test
    public void canDelete(){
        Person savedPerson = repo.save(new Person("Anna", "Zholmogorova", ZonedDateTime.of(1990, 02, 18, 22, 40, 25, 6, ZoneId.of("+3")),new BigDecimal("1111.11")));
        long startCount = repo.count();
        repo.delete(savedPerson);
        long endCount = repo.count();
        assertThat(endCount).isEqualTo(startCount-1);
    }

    @Test
    public void canDeleteMultipleWithSimpleStatement(){
        Person savedPerson1 = repo.save(new Person("Anna", "Kholmogorova", ZonedDateTime.of(1990, 02, 18, 22, 40, 25, 6, ZoneId.of("+3")),new BigDecimal("1111.11")));
        Person savedPerson2= repo.save(new Person ("Alexandr", "Vasechkin", ZonedDateTime.of(1996, 07, 18, 22, 40, 25,6, ZoneId.of("+3")),new BigDecimal("1111.11")));
        long startCount = repo.count();
        repo.deleteMultipleWithSimpleStatement(savedPerson1,savedPerson2);
        long endCount = repo.count();
        assertThat(endCount).isEqualTo(startCount-2);
    }


    @Test
    public void canUpdate() {
        Person savedPerson = repo.save(new Person("Anna", "Kholmogorova", ZonedDateTime.of(1990, 02, 18, 22, 40, 25, 6, ZoneId.of("+3")),new BigDecimal("1111.11")));
        Person p1= repo.findById(savedPerson.getId()).get();
        savedPerson.setSalary(new BigDecimal("73654.66"));
        repo.update(savedPerson);
        Person p2= repo.findById(savedPerson.getId()).get();
        assertThat(p1.getSalary()).isNotEqualTo(p2.getSalary());
    }

    @Test
    @Disabled
    public void loadData() throws IOException, SQLException {
        Files.lines(Path.of("/Users/VP/Desktop/database for squirrel/Hr5m.csv"))
                .skip(1)
                .limit(10000)
                .map(l -> l.split(","))
                .map(a -> {
                    LocalDate dob = LocalDate.parse(a[10], DateTimeFormatter.ofPattern("M/d/yyyy"));
                    LocalTime tob = LocalTime.parse(a[11], DateTimeFormatter.ofPattern("hh:mm:ss a"));
                    LocalDateTime dtob = LocalDateTime.of(dob,tob);
                    ZonedDateTime zdtob = ZonedDateTime.of(dtob, ZoneId.of("+0"));
                    Person person = new Person(a[2], a[4], zdtob, new BigDecimal(a[25]));
                    person.setEmail(a[6]);
                    return person;
                })
                .forEach(repo::save);
        connection.commit();
    }

}
