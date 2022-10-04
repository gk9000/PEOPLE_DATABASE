package com.gennadykulikov.peopledb.repository;

import com.gennadykulikov.peopledb.annotations.SQL;
import com.gennadykulikov.peopledb.model.Address;
import com.gennadykulikov.peopledb.model.Region;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AddressRepository extends CRUDRepository<Address>{

    public AddressRepository(Connection connection) {
        super(connection);
    }

    @Override
    @SQL(value="""
    INSERT INTO ADDRESSES (ADDRESS1, ADDRESS2, CITY, STATE, POSTCODE, COUNTY, REGION, COUNTRY) 
    VALUES (?,?,?,?,?,?,?,?)""")
    void mapForSave(Address address, PreparedStatement ps) throws SQLException {
        ps.setString(1, address.getAddress1());
        ps.setString(2, address.getAddress2());
        ps.setString(3, address.getCity());
        ps.setString(4, address.getState());
        ps.setString(5, address.getPostalcode());
        ps.setString(6, address.getCounty());
        ps.setString(7, address.getRegion());
        ps.setString(8, address.getCountry());
    }


    @Override
    @SQL(value= """
            SELECT ADDRESS_ID, ADDRESS1, ADDRESS2, CITY, STATE, POSTCODE, COUNTY, REGION, COUNTRY
            FROM ADDRESSES
            WHERE ADDRESS_ID=?
            """)
    Address mapForFindById(long id, PreparedStatement ps) throws SQLException {
        ResultSet rs = ps.executeQuery();
        Address address = null;
        while (rs.next()) {
            address = extractEntityFromResultSet(rs);
        }
        return address;
    }

    @Override
    Address mapForFindAll(ResultSet rs, PreparedStatement ps) throws SQLException {
        return null;
    }

    @Override
    String getCountSql() {
        return null;
    }

    @Override
    protected String getDeleteSql() {
        return null;
    }

    @Override
    protected String getDeleteMultipleWithSimpleStatement() {
        return null;
    }

    @Override
    Address mapForUpdate(PreparedStatement ps, Address entity) throws SQLException {
        return null;
    }

    @Override
    Address extractEntityFromResultSet(ResultSet rs) throws SQLException {
        long id = rs.getLong("ADDRESS_ID");
        String address1 = rs.getString("ADDRESS1");
        String address2 = rs.getString("ADDRESS2");
        String city = rs.getString("CITY");
        String state = rs.getString("STATE");
        String postcode = rs.getString("POSTCODE");
        String county = rs.getString("COUNTY");
        Region region = Region.valueOf(rs.getString("REGION").toUpperCase());
        String country = rs.getString("COUNTRY");
        Address address = new Address(id,address1,address2,city,county,state,postcode, region,country);
        return address;
    }
}
