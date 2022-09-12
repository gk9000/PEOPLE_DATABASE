package com.gennadykulikov.peopledb.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PersonTest {
    @Test
    public void testForEquality(){
        Person p1 = new Person("aa","bb", ZonedDateTime.of(2000,10,10,1,1,1,1, ZoneId.of("+0")),new BigDecimal("1111.11"));
        Person p2 = new Person("aa","bb", ZonedDateTime.of(2000,10,10,1,1,1,1, ZoneId.of("+0")),new BigDecimal("1111.11"));
        assertThat(p1).isEqualTo(p2);
    }

}