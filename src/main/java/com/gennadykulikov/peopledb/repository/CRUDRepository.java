package com.gennadykulikov.peopledb.repository;

import com.gennadykulikov.peopledb.annotations.SQL;
import com.gennadykulikov.peopledb.model.Entity;
import com.gennadykulikov.peopledb.model.Person;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

abstract class CRUDRepository <T extends Entity> {
    protected Connection connection;

    public CRUDRepository(Connection connection) {
        this.connection = connection;
    }



    public T save(T entity) {
        try{
            PreparedStatement ps = connection.prepareStatement(getSqlByAnnotation("mapForSave"), Statement.RETURN_GENERATED_KEYS);
            mapForSave(entity, ps);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            while (rs.next()){
                long id = rs.getLong(1);
                entity.setId(id);
            }
        }  catch (SQLException e){
            e.printStackTrace();
        }
        return entity;
    }

    abstract void mapForSave(T entity, PreparedStatement ps) throws SQLException;
//     String getSaveSql(){
//         return "";
//     };


    abstract T mapForFindById(long id, PreparedStatement ps) throws SQLException;

    public Optional<T> findById(Long id) {
        T foundEntity=null;
        try {
            PreparedStatement ps = connection.prepareStatement(getSqlByAnnotation("mapForFindById"));
            ps.setLong(1,id);
            ps.setLong(2,id);
            ps.setLong(3,id);
            foundEntity = mapForFindById(id, ps);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(foundEntity);
    }

//    abstract String getFindByIdSql();




    public List<T> findAll(){
        T entity=null;
        List <T> entities = new ArrayList<>();
        try{
            PreparedStatement ps = connection.prepareStatement(getSqlByAnnotation("mapForFindAll"));
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                entity = mapForFindAll(rs, ps);
                entities.add(entity);
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return entities;
    }



    abstract T mapForFindAll(ResultSet rs, PreparedStatement ps) throws SQLException;
//    abstract String getFindAllSql();



    public long count () {
        long count = 0;
        try {
            PreparedStatement ps = connection.prepareStatement(getCountSql());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    abstract String getCountSql();



    public void delete (T entity){
        try {
            PreparedStatement ps = connection.prepareStatement(getDeleteSql());
            ps.setLong(1, entity.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteMultipleWithSimpleStatement (T ...entities){
        try {
            Statement statement = connection.createStatement();
            String ids = Arrays.stream(entities).map(T::getId).map(String::valueOf).collect(Collectors.joining(","));
            statement.executeUpdate(getDeleteMultipleWithSimpleStatement().replace(":ids", ids));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected abstract String getDeleteSql();
    protected abstract String getDeleteMultipleWithSimpleStatement();



    public void update (T entity){
        try {
            PreparedStatement ps = connection.prepareStatement(getSqlByAnnotation("mapForUpdate"));
            mapForUpdate(ps,entity);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//     abstract String getUpdateSql();
    abstract T mapForUpdate(PreparedStatement ps,T entity) throws SQLException;




//    private String getSqlByAnnotation(String methodName, Supplier<String> sqlGetter){
//        return Arrays.stream(this.getClass().getDeclaredMethods())
//                .filter(m -> methodName.equals(m.getName()))
//                .map(m -> m.getAnnotation(SQL.class))
//                .map(SQL::value)
//                .findFirst().orElseGet(sqlGetter);
//    }

    private String getSqlByAnnotation(String methodName){
        return Arrays.stream(this.getClass().getDeclaredMethods())
                .filter(m -> methodName.equals(m.getName()))
                .map(m -> m.getAnnotation(SQL.class))
                .map(SQL::value)
                .findFirst().orElse("");
    }

    abstract Entity extractEntityFromResultSet(ResultSet rs) throws SQLException;



}
