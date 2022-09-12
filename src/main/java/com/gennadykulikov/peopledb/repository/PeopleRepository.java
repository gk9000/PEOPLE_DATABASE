package com.gennadykulikov.peopledb.repository;

import com.gennadykulikov.peopledb.annotations.SQL;
import com.gennadykulikov.peopledb.model.Address;
import com.gennadykulikov.peopledb.model.Person;
import com.gennadykulikov.peopledb.model.Region;

import java.math.BigDecimal;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

public class PeopleRepository extends CRUDRepository<Person>{
    private AddressRepository addressRepository = null;

    public PeopleRepository(Connection connection) {
        super(connection);
        addressRepository = new AddressRepository(connection);
    }

    @Override
    @SQL(value="INSERT INTO PEOPLE (FIRST_NAME, LAST_NAME, DOB, SALARY, EMAIL, HOME_ADDRESS_CODE, BUSINESS_ADDRESS_CODE) VALUES (?,?,?,?,?,?,?)")
    void mapForSave(Person person, PreparedStatement ps) throws SQLException {
            ps.setString(1, person.getFirstName());
            ps.setString(2, person.getLastName());
            ps.setTimestamp(3, convertDobToTimestamp(person));
            ps.setBigDecimal(4, person.getSalary());
            ps.setString(5, person.getEmail());

            ps.setObject(6, null);
            ps.setObject(7, null);
            if(person.getHomeAddress().isPresent()){
                Address savedAddress = addressRepository.save(person.getHomeAddress().get());
                ps.setLong(6, savedAddress.getId());
            }
            if(person.getBusinessAddress().isPresent()){
                Address savedAddress = addressRepository.save(person.getBusinessAddress().get());
                ps.setLong(7, savedAddress.getId());
            }
    }




    @Override
    @SQL(value = """
            SELECT
                        P.ID, P.FIRST_NAME, P.LAST_NAME, P.DOB, P.SALARY, P.HOME_ADDRESS_CODE, P.BUSINESS_ADDRESS_CODE,
                        A.ADDRESS_ID, A.ADDRESS1, A.ADDRESS2, A.CITY,A.POSTCODE, A.COUNTY, A.REGION, A.COUNTRY, A.STATE
                        FROM PEOPLE AS P
                        LEFT OUTER JOIN
                        ADDRESSES AS A ON P.HOME_ADDRESS_CODE=A.ADDRESS_ID
                        WHERE P.ID=? AND P.HOME_ADDRESS_CODE IS NOT NULL
                        UNION
                        SELECT
                        P.ID, P.FIRST_NAME, P.LAST_NAME, P.DOB, P.SALARY, P.HOME_ADDRESS_CODE, P.BUSINESS_ADDRESS_CODE,
                        A.ADDRESS_ID, A.ADDRESS1, A.ADDRESS2, A.CITY,A.POSTCODE, A.COUNTY, A.REGION, A.COUNTRY, A.STATE
                        FROM PEOPLE AS P
                        LEFT OUTER JOIN
                        ADDRESSES AS A ON P.BUSINESS_ADDRESS_CODE=A.ADDRESS_ID
                        WHERE P.ID=? AND P.BUSINESS_ADDRESS_CODE IS NOT NULL
                        UNION
                        SELECT
                        P.ID, P.FIRST_NAME, P.LAST_NAME, P.DOB, P.SALARY, P.HOME_ADDRESS_CODE, P.BUSINESS_ADDRESS_CODE,
                        A.ADDRESS_ID, A.ADDRESS1, A.ADDRESS2, A.CITY,A.POSTCODE, A.COUNTY, A.REGION, A.COUNTRY, A.STATE
                        FROM PEOPLE AS P
                        LEFT OUTER JOIN
                        ADDRESSES AS A ON P.HOME_ADDRESS_CODE=A.ADDRESS_ID
                        WHERE P.ID=? 
            """)
    Person mapForFindById(long id, PreparedStatement ps) throws SQLException {
        ResultSet rs = ps.executeQuery();
        Person person = null;
        Long tempId = 0L;
        Optional<Address> tempHomeAddress = Optional.empty();
        while (rs.next()) {
            person = extractEntityFromResultSet(rs);
            if(Objects.equals(person.getId(), tempId)){
                person.setHomeAddress(tempHomeAddress.orElse(null));
                return person;
            }
            tempId = person.getId();
            tempHomeAddress = person.getHomeAddress();
        }
        return person;
    }


    @Override
    @SQL (value= """
            SELECT
            P.ID, P.FIRST_NAME, P.LAST_NAME, P.DOB, P.SALARY, P.HOME_ADDRESS_CODE, P.BUSINESS_ADDRESS_CODE,
            A.ADDRESS_ID, A.ADDRESS1, A.ADDRESS2, A.CITY,A.POSTCODE, A.COUNTY, A.REGION, A.COUNTRY, A.STATE
            FROM PEOPLE AS P
            LEFT OUTER JOIN
            ADDRESSES AS A ON P.HOME_ADDRESS_CODE=A.ADDRESS_ID
            """)
    Person mapForFindAll(ResultSet rs, PreparedStatement ps) throws SQLException {
        Person person = extractEntityFromResultSet(rs);
        return person;
    }



    @Override
    String getCountSql() {
        return "SELECT COUNT (*) FROM PEOPLE";
    }



    @Override
    protected String getDeleteSql() {
        return "DELETE FROM PEOPLE WHERE ID=?";
    }

    @Override
    protected String getDeleteMultipleWithSimpleStatement() {
        return "DELETE FROM PEOPLE WHERE ID IN (:ids)";
    }



    @Override
    @SQL(value="UPDATE PEOPLE SET FIRST_NAME=?,LAST_NAME=?,DOB=?,SALARY=? WHERE ID=?")
    Person mapForUpdate(PreparedStatement ps, Person person) throws SQLException {
        ps.setString(1, person.getFirstName());
        ps.setString(2, person.getLastName());
        ps.setTimestamp(3, convertDobToTimestamp(person));
        ps.setBigDecimal(4, person.getSalary());
        ps.setLong(5, person.getId());
        ps.executeUpdate();
        return person;
    }


    @Override
    public Person extractEntityFromResultSet(ResultSet rs) throws SQLException {
        long personId = rs.getLong("ID");
        String firstName = rs.getString("FIRST_NAME");
        String lastName = rs.getString("LAST_NAME");
        ZonedDateTime dob = ZonedDateTime.of(rs.getTimestamp("DOB").toLocalDateTime(), ZoneId.of("+0"));
        BigDecimal salary = rs.getBigDecimal("SALARY");
        Person person = new Person(firstName, lastName, dob, salary);
        person.setId(personId);

        if (rs.getObject("HOME_ADDRESS_CODE") == null && rs.getObject("BUSINESS_ADDRESS_CODE") == null) {
            return person;
        }

        if (rs.getLong("HOME_ADDRESS_CODE") != 0) {
            long addressId = rs.getLong("HOME_ADDRESS_CODE");
            Address address = extractAddress(rs, addressId);
            person.setHomeAddress(address);
        }

        if (rs.getLong("BUSINESS_ADDRESS_CODE") != 0){
            long addressId = rs.getLong("BUSINESS_ADDRESS_CODE");
            Address address = extractAddress(rs, addressId);
            person.setBusinessAddress(address);
        }
        return person;
       }

    private Address extractAddress(ResultSet rs, long addressId) throws SQLException {
        String address1 = rs.getString("ADDRESS1");
        String address2 = rs.getString("ADDRESS2");
        String city = rs.getString("CITY");
        String state = rs.getString("STATE");
        String postcode = rs.getString("POSTCODE");
        String county = rs.getString("COUNTY");
        Region region;
        if (rs.getString("REGION") != null) {
            region = Region.valueOf(rs.getString("REGION").toUpperCase());
        } else {
            region = null;
        }
        String country = rs.getString("COUNTRY");
        Address address = new Address(addressId,address1,address2,city,county,state,postcode,region,country);
        return address;
    }

    private Timestamp convertDobToTimestamp(Person person){
           return Timestamp.valueOf(person.getDob().withZoneSameInstant(ZoneId.of("+0")).toLocalDateTime());
       }

    }


//@SQL (value= """
//           SELECT
//           P.ID, P.FIRST_NAME, P.LAST_NAME, P.DOB, P.SALARY, P.HOME_ADDRESS_CODE, P.BUSINESS_ADDRESS_CODE,
//           A.ADDRESS_ID, A.ADDRESS1, A.ADDRESS2, A.CITY,A.POSTCODE, A.COUNTY, A.REGION, A.COUNTRY, A.STATE
//           FROM PEOPLE AS P
//           LEFT OUTER JOIN
//           ADDRESSES AS A ON P.HOME_ADDRESS_CODE=A.ADDRESS_ID OR P.BUSINESS_ADDRESS_CODE=A.ADDRESS_ID
//           WHERE P.ID=?""")



//    SELECT
//            P.ID, P.FIRST_NAME, P.LAST_NAME, P.DOB, P.SALARY, P.HOME_ADDRESS_CODE, P.BUSINESS_ADDRESS_CODE,
//            A.ADDRESS_ID, A.ADDRESS1, A.ADDRESS2, A.CITY,A.POSTCODE, A.COUNTY, A.REGION, A.COUNTRY, A.STATE
//            FROM PEOPLE AS P
//            LEFT OUTER JOIN
//            ADDRESSES AS A ON P.HOME_ADDRESS_CODE=A.ADDRESS_ID
//            WHERE P.ID=? AND P.HOME_ADDRESS_CODE IS NOT NULL
//            UNION
//            SELECT
//            P.ID, P.FIRST_NAME, P.LAST_NAME, P.DOB, P.SALARY, P.HOME_ADDRESS_CODE, P.BUSINESS_ADDRESS_CODE,
//            A.ADDRESS_ID, A.ADDRESS1, A.ADDRESS2, A.CITY,A.POSTCODE, A.COUNTY, A.REGION, A.COUNTRY, A.STATE
//            FROM PEOPLE AS P
//            LEFT OUTER JOIN
//            ADDRESSES AS A ON P.BUSINESS_ADDRESS_CODE=A.ADDRESS_ID
//            WHERE P.ID=? AND P.BUSINESS_ADDRESS_CODE IS NOT NULL
//            UNION
//            SELECT
//            P.ID, P.FIRST_NAME, P.LAST_NAME, P.DOB, P.SALARY, P.HOME_ADDRESS_CODE, P.BUSINESS_ADDRESS_CODE,
//            A.ADDRESS_ID, A.ADDRESS1, A.ADDRESS2, A.CITY,A.POSTCODE, A.COUNTY, A.REGION, A.COUNTRY, A.STATE
//            FROM PEOPLE AS P
//            LEFT OUTER JOIN
//            ADDRESSES AS A ON P.HOME_ADDRESS_CODE=A.ADDRESS_ID
//            WHERE P.ID=?