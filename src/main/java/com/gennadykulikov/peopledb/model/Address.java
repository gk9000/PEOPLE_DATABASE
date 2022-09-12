package com.gennadykulikov.peopledb.model;

import java.util.Optional;

public class Address implements Entity {
    private Long id;
    private String address1;
    private String address2;
    private String city;
    private String county;
    private String state;
    private String postalcode;
    private String region;
    private String country;

    public Address(Long id, String address1, String address2, String city, String county, String state, String postalcode, Region region, String country) {
        this.id = id;
        this.address1 = address1;
        this.address2 = address2;
        this.city = city;
        this.county = county;
        this.state = state;
        this.postalcode = postalcode;
        if (region != null) {
            this.region = region.toString();
        } else {
            this.region = null;
        }
        this.country = country;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalcode() {
        return postalcode;
    }

    public void setPostalcode(String postalcode) {
        this.postalcode = postalcode;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return "Address{" +
                "id=" + id +
                ", address1='" + address1 + '\'' +
                ", address2='" + address2 + '\'' +
                ", city='" + city + '\'' +
                ", county='" + county + '\'' +
                ", state='" + state + '\'' +
                ", postalcode='" + postalcode + '\'' +
                ", region='" + region + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}


