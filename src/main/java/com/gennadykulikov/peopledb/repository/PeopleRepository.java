package com.gennadykulikov.peopledb.repository;

import com.gennadykulikov.peopledb.annotations.SQL;
import com.gennadykulikov.peopledb.model.Address;
import com.gennadykulikov.peopledb.model.Person;
import com.gennadykulikov.peopledb.model.Region;
import org.h2.value.Value;

import java.math.BigDecimal;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PeopleRepository extends CRUDRepository<Person>{
    private AddressRepository addressRepository = null;
    Long personId;
    private Map<String, Integer> aliasConIdxMap = new HashMap<>();

    public PeopleRepository(Connection connection) {
        super(connection);
        addressRepository = new AddressRepository(connection);
    }

    @Override
    @SQL(value="INSERT INTO PEOPLE (FIRST_NAME, LAST_NAME, DOB, SALARY, EMAIL, HOME_ADDRESS_CODE, BUSINESS_ADDRESS_CODE, PARENT_ID) VALUES (?,?,?,?,?,?,?,?)")
    void mapForSave(Person person, PreparedStatement ps) throws SQLException {
            ps.setString(1, person.getFirstName());
            ps.setString(2, person.getLastName());
            ps.setTimestamp(3, convertDobToTimestamp(person));
            ps.setBigDecimal(4, person.getSalary());
            ps.setString(5, person.getEmail());
            if(person.getParent().isPresent()){
                ps.setLong(8, person.getParent().get().getId());
            } else {
                ps.setObject(8,null);
            }


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
    protected void postSave(Person entity, long id) {
        entity.getChildren().stream()
                .forEach(this::save);
    }

    @Override
    @SQL(value = """
            SELECT
            P.ID AS PARENT_ID, P.FIRST_NAME AS PARENT_FIRST_NAME, P.LAST_NAME AS PARENT_LAST_NAME, 
            P.DOB AS PARENT_DOB, P.SALARY AS PARENT_SALARY, P.HOME_ADDRESS_CODE AS PARENT_HOME_ADDRESS_CODE, 
            P.BUSINESS_ADDRESS_CODE AS PARENT_BUSINESS_ADDRESS_CODE, P.PARENT_ID AS PARENT_PARENT_ID,
            C.ID AS CHILD_ID, C.FIRST_NAME AS CHILD_FIRST_NAME, C.LAST_NAME AS CHILD_LAST_NAME, 
            C.DOB AS CHILD_DOB, C.SALARY AS CHILD_SALARY, C.HOME_ADDRESS_CODE AS CHILD_HOME_ADDRESS_CODE, 
            C.BUSINESS_ADDRESS_CODE AS CHILD_BUSINESS_ADDRESS_CODE, C.PARENT_ID AS CHILD_PARENT_ID,
            H.ADDRESS_ID AS HOME_ADDRESS_ID, H.ADDRESS1 AS HOME_ADDRESS1, H.ADDRESS2 AS HOME_ADDRESS2,
            H.CITY AS HOME_CITY, H.POSTCODE AS HOME_POSTCODE, H.COUNTY AS HOME_COUNTY,
            H.REGION AS HOME_REGION, H.COUNTRY AS HOME_COUNTRY, H.STATE AS HOME_STATE,
            B.ADDRESS_ID AS BUSINESS_ADDRESS_ID, B.ADDRESS1 AS BUSINESS_ADDRESS1, B.ADDRESS2 AS BUSINESS_ADDRESS2,
            B.CITY AS BUSINESS_CITY, B.POSTCODE AS BUSINESS_POSTCODE, B.COUNTY AS BUSINESS_COUNTY,
            B.REGION AS BUSINESS_REGION, B.COUNTRY AS BUSINESS_COUNTRY, B.STATE AS BUSINESS_STATE,
            FROM PEOPLE AS P
            LEFT OUTER JOIN PEOPLE AS C ON P.ID=C.PARENT_ID
            LEFT OUTER JOIN ADDRESSES AS H ON P.HOME_ADDRESS_CODE=H.ADDRESS_ID 
            LEFT OUTER JOIN ADDRESSES AS B ON P.BUSINESS_ADDRESS_CODE=B.ADDRESS_ID 
            WHERE P.ID=?
            """)
    Person mapForFindById(long id, PreparedStatement ps) throws SQLException {
        ResultSet rs = ps.executeQuery();
        Person person = null;
        while (rs.next()) {
            person = extractEntityFromResultSet(rs);
        }
        return person;
    }


    @Override
    @SQL (value= """
            SELECT 
            P.ID AS PARENT_ID, P.FIRST_NAME AS PARENT_FIRST_NAME, P.LAST_NAME AS PARENT_LAST_NAME, 
            P.DOB AS PARENT_DOB, P.SALARY AS PARENT_SALARY, P.EMAIL AS PARENT_EMAIL 
            FROM PEOPLE AS P
            FETCH FIRST 100 ROWS ONLY
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
        Person parent=null;
        do {
            Person temporaryParent = extractPersonFromResultSet(rs, "PARENT_").get();
            if (parent==null){
                parent = temporaryParent;
            } if(!parent.equals(temporaryParent)){
                rs.previous();
                break;
            }

            Optional<Person> child = extractPersonFromResultSet(rs, "CHILD_");

            Address homeAddress = extractAddress(rs, "HOME_");
            Address businessAddress = extractAddress(rs, "BUSINESS_");

            parent.setHomeAddress(homeAddress);
            parent.setBusinessAddress(businessAddress);
            child.ifPresent(parent::addChild);
        } while (rs.next());
        return parent;
       }

    private Optional<Person> extractPersonFromResultSet(ResultSet rs, String aliasPrefix) throws SQLException {
        personId = getValueByAlias(aliasPrefix + "ID", rs, Long.class);
        if(personId==null){return Optional.empty();}
        String firstName = getValueByAlias(aliasPrefix + "FIRST_NAME", rs, String.class);
        String lastName = getValueByAlias(aliasPrefix + "LAST_NAME", rs, String.class);
        ZonedDateTime dob = ZonedDateTime.of(getValueByAlias(aliasPrefix + "DOB", rs, Timestamp.class).toLocalDateTime(), ZoneId.of("+0"));
        BigDecimal salary = getValueByAlias(aliasPrefix + "SALARY", rs, BigDecimal.class);
        Person person = new Person(firstName, lastName, dob, salary);
        person.setId(personId);
        return Optional.of(person);
    }

    private Address extractAddress(ResultSet rs, String aliasPrefix) throws SQLException {
        Long addressId = getValueByAlias (aliasPrefix + "ADDRESS_ID", rs, Long.class);
        if(addressId == null) return null;
        String address1 = getValueByAlias(aliasPrefix + "ADDRESS1", rs, String.class);
        String address2 = getValueByAlias(aliasPrefix + "ADDRESS2", rs, String.class);
        String city = getValueByAlias(aliasPrefix + "CITY", rs, String.class);
        String state = getValueByAlias(aliasPrefix + "STATE", rs, String.class);
        String postcode = getValueByAlias(aliasPrefix + "POSTCODE", rs, String.class);
        String county = getValueByAlias(aliasPrefix + "COUNTY", rs, String.class);
        Region region = Region.valueOf(getValueByAlias(aliasPrefix + "REGION", rs, String.class).toUpperCase());
        String country = getValueByAlias(aliasPrefix + "COUNTRY", rs, String.class);
        Address address = new Address(addressId,address1,address2,city,county,state,postcode,region,country);
        return address;
    }

    private <T>T getValueByAlias(String alias, ResultSet rs, Class<T> clazz) throws SQLException{
           int columnCount = rs.getMetaData().getColumnCount();
           int foundIndex = getIndexForAlias(alias, rs, columnCount);
           return foundIndex==0 ? null : (T) rs.getObject(foundIndex);
    }

    private int getIndexForAlias(String alias, ResultSet rs, int columnCount) throws SQLException {
        Integer foundIndex = aliasConIdxMap.getOrDefault(alias,0);
        if (foundIndex==0) {
            for(int columnIndex = 1; columnIndex<= columnCount; columnIndex++){
                if(alias.equals(rs.getMetaData().getColumnLabel(columnIndex))){
                         foundIndex = columnIndex;
                         aliasConIdxMap.put(alias,foundIndex);
                         break;
                }
            }
        }
        return foundIndex;
    }

    private Timestamp convertDobToTimestamp(Person person){
           return Timestamp.valueOf(person.getDob().withZoneSameInstant(ZoneId.of("+0")).toLocalDateTime());
       }

    }
