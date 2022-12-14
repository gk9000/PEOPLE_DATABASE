package com.gennadykulikov.peopledb.repository;

import com.gennadykulikov.peopledb.model.Address;
import com.gennadykulikov.peopledb.model.Person;
import com.gennadykulikov.peopledb.model.Region;
import org.junit.jupiter.api.*;

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
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PeopleRepositoryTests {

    private Connection connection;
    private PeopleRepository repo;

    @BeforeAll
    void setUp() throws SQLException {
        connection = DriverManager.getConnection(("jdbc:h2:~/peopleproject;TRACE_LEVEL_SYSTEM_OUT=0").replace("~", System.getProperty("user.home")));
        connection.setAutoCommit(false);
        repo = new PeopleRepository(connection);
    }

    @AfterEach
    void cleanupDatabase() throws SQLException {
        connection.rollback();
    }

    @AfterAll
    void tearDown() throws SQLException {
        if(connection != null){
            connection.close();
        }
    }

    @Test
    public void canSaveOnePerson() throws SQLException {
        Person sergei = new Person ("Sergei", "Kukushkin", ZonedDateTime.of(1990, 02, 18, 22, 40, 25,0, ZoneId.of("+3")),new BigDecimal("1111.11"));
        Person savedPerson = repo.save(sergei);
        assertThat(savedPerson.getId()).isGreaterThan(0);
    }

    @Test
    public void canSaveTwoPeople() throws SQLException {
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
    public void canSavePersonWithChildren() throws SQLException {
        Person alex = new Person("Alexandr", "Vasechkin", ZonedDateTime.of(1986, 07, 18, 22, 40, 25, 6, ZoneId.of("+3")), new BigDecimal("1111.11"));
        alex.addChild(new Person("Michail", "Vasechkin", ZonedDateTime.of(2010, 2, 20, 5, 33, 0, 0, ZoneId.of("+3")), new BigDecimal("0")));
        alex.addChild(new Person("Olga", "Vasechkina", ZonedDateTime.of(2014, 9,30,5,33,0,0, ZoneId.of("+3")), new BigDecimal("0")));
        alex.addChild(new Person("Svetlana", "Vasechkina", ZonedDateTime.of(2017, 4,30,5,33,0,0, ZoneId.of("+3")), new BigDecimal("0")));
        alex.addChild(new Person("Maksim", "Vasechkin", ZonedDateTime.of(2020, 6, 30, 5, 33, 0, 0, ZoneId.of("+3")), new BigDecimal("0")));
        Person savedPerson = repo.save(alex);
        assertThat(savedPerson.getChildren().size()).isEqualTo(4);
        savedPerson.getChildren().stream()
                .map(Person::getId)
                .forEach(id -> assertThat(id).isGreaterThan(0));
    }

    @Test
    public void canFindPersonById() throws SQLException {
        Person savedPerson = repo.save(new Person("Test3", "Test3", ZonedDateTime.of(1990, 02, 18, 22, 10, 10, 100000, ZoneId.of("+2")), new BigDecimal("2211.33")));
        Person foundPerson = repo.findById(savedPerson.getId()).get();
        assertThat(savedPerson).isEqualTo(foundPerson);
    }

    @Test
    public void canFindPersonByIdWithHomeAddress() throws SQLException {
        Person elena = new Person ("Elena", "Maksimova", ZonedDateTime.of(1990, 02, 18, 22, 40, 25,6, ZoneId.of("+3")),new BigDecimal("1111.11"));
        Address address = new Address(null, "33 Big Str.", "44 Small Str.", "Iron Town", "Fulton County", "CA", "642-643", Region.SOUTHWEST, "US");
        elena.setHomeAddress(address);
        Person savedPerson = repo.save(elena);
        Person foundPerson = repo.findById(savedPerson.getId()).get();
        assertThat(foundPerson.getHomeAddress().get().getState()).isEqualTo("CA");
    }

    @Test
    public void canFindPersonByIdWithBusinessAddress() throws SQLException {
        Person elena = new Person ("Elena", "Maksimova", ZonedDateTime.of(1990, 02, 18, 22, 40, 25,6, ZoneId.of("+3")),new BigDecimal("1111.11"));
        Address address = new Address(null, "33 Big Str.", "44 Small Str.", "Iron Town", "Fulton County", "NY", "642-643", Region.SOUTHWEST, "US");
        elena.setBusinessAddress(address);
        Person savedPerson = repo.save(elena);
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
        Person savedPerson = repo.save(elena);
        Person foundPerson = repo.findById(savedPerson.getId()).get();
        assertThat(foundPerson.getHomeAddress().get().getState()).isEqualTo("NY");
        assertThat(foundPerson.getBusinessAddress().get().getState()).isEqualTo("FL");
    }

    @Test
    public void canFindPersonByIdWithChildren() throws SQLException {
        Person alex = new Person("Alexandr", "Vasechkin", ZonedDateTime.of(1986, 07, 18, 22, 40, 25, 6, ZoneId.of("+3")), new BigDecimal("1111.11"));
        alex.addChild(new Person("Michail", "Vasechkin", ZonedDateTime.of(2010, 2, 20, 5, 33, 0, 0, ZoneId.of("+3")), new BigDecimal("0")));
        alex.addChild(new Person("Olga", "Vasechkina", ZonedDateTime.of(2014, 9, 30, 5, 33, 0, 0, ZoneId.of("+3")), new BigDecimal("0")));
        alex.addChild(new Person("Svetlana", "Vasechkina", ZonedDateTime.of(2017, 4, 30, 5, 33, 0, 0, ZoneId.of("+3")), new BigDecimal("0")));
        alex.addChild(new Person("Maksim", "Vasechkin", ZonedDateTime.of(2020, 6, 30, 5, 33, 0, 0, ZoneId.of("+3")), new BigDecimal("0")));
        Person savedPerson = repo.save(alex);
        Person foundPerson = repo.findById(savedPerson.getId()).get();
        assertThat(foundPerson.getChildren().stream()
                .map(Person::getFirstName).collect(Collectors.toSet()))
                .contains("Michail", "Olga", "Svetlana", "Maksim");
    }

    @Test
    @Disabled
    public void canFindAll() throws SQLException {
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
        assertThat(people.size()).isGreaterThanOrEqualTo(10);
    }


    @Test
    public void testPersonIdNotFound() throws SQLException {
        Person foundPerson = repo.findById(-1L).orElse(null);
        assertThat(foundPerson).isNull();
    }

    @Test
    public void canGetCount() throws SQLException {
        long startCount = repo.count();
        repo.save(new Person("Test", "Test", ZonedDateTime.of(1990, 02, 18, 22, 10, 10,100000, ZoneId.of("+2")),new BigDecimal("1111.11")));
        repo.save(new Person("Test1", "Test", ZonedDateTime.of(1990, 02, 18, 22, 10, 10, 100000, ZoneId.of("+2")),new BigDecimal("1111.11")));
        long endCount = repo.count();
        assertThat(endCount).isEqualTo(startCount+2);
    }

    @Test
    public void canDelete() throws SQLException {
        Person savedPerson = repo.save(new Person("Anna", "Zholmogorova", ZonedDateTime.of(1990, 02, 18, 22, 40, 25, 6, ZoneId.of("+3")),new BigDecimal("1111.11")));
        long startCount = repo.count();
        repo.delete(savedPerson);
        long endCount = repo.count();
        assertThat(endCount).isEqualTo(startCount-1);
    }

    @Test
    public void canDeleteMultipleWithSimpleStatement() throws SQLException {
        Person savedPerson1 = repo.save(new Person("Anna", "Kholmogorova", ZonedDateTime.of(1990, 02, 18, 22, 40, 25, 6, ZoneId.of("+3")),new BigDecimal("1111.11")));
        Person savedPerson2= repo.save(new Person ("Alexandr", "Vasechkin", ZonedDateTime.of(1996, 07, 18, 22, 40, 25,6, ZoneId.of("+3")),new BigDecimal("1111.11")));
        long startCount = repo.count();
        repo.deleteMultipleWithSimpleStatement(savedPerson1,savedPerson2);
        long endCount = repo.count();
        assertThat(endCount).isEqualTo(startCount-2);
    }


    @Test
    public void canUpdate() throws SQLException {
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
                .limit(50000)
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
