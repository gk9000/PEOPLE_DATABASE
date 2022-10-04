package com.gennadykulikov.peopledb.repository;

import com.gennadykulikov.peopledb.annotations.SQL;
import com.gennadykulikov.peopledb.exception.DataException;
import com.gennadykulikov.peopledb.model.Entity;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

abstract class CRUDRepository <T extends Entity> {
    protected Connection connection;
    private PreparedStatement savePS;
    private PreparedStatement findByIdPS;


    public CRUDRepository(Connection connection) {
        try {
            this.connection = connection;
            savePS = connection.prepareStatement(getSqlByAnnotation("mapForSave"), Statement.RETURN_GENERATED_KEYS);
            findByIdPS = connection.prepareStatement(getSqlByAnnotation("mapForFindById"));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataException("Unable to create prepared statements for CRUDRepository", e);
        }
    }


    public T save(T entity) {
        try{
            mapForSave(entity, savePS);
            savePS.executeUpdate();
            ResultSet rs = savePS.getGeneratedKeys();
            while (rs.next()){
                long id = rs.getLong(1);
                entity.setId(id);
                postSave(entity,id);
            }
        }  catch (SQLException e){
            e.printStackTrace();
        }
        return entity;
    }

    protected void postSave(T entity, long id) {}

    abstract void mapForSave(T entity, PreparedStatement ps) throws SQLException;

    abstract T mapForFindById(long id, PreparedStatement ps) throws SQLException;

    public Optional<T> findById(Long id) {
        T foundEntity=null;
        try {
            findByIdPS.setLong(1,id);
            foundEntity = mapForFindById(id, findByIdPS);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataException("Unable to create prepared statements for CRUDRepository", e);
        }
        return Optional.ofNullable(foundEntity);
    }


    public List<T> findAll(){
        T entity=null;
        List <T> entities = new ArrayList<>();
        try{
            PreparedStatement findAllPS = connection.prepareStatement(getSqlByAnnotation("mapForFindAll"),
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = findAllPS.executeQuery();
            while(rs.next()){
                entity = mapForFindAll(rs, findAllPS);
                entities.add(entity);
            }
        } catch (SQLException e){
            e.printStackTrace();
            throw new DataException("Unable to create prepared statements for CRUDRepository", e);
        }
        return entities;
    }



    abstract T mapForFindAll(ResultSet rs, PreparedStatement ps) throws SQLException;



    public long count () throws SQLException {
       PreparedStatement countPS = connection.prepareStatement(getCountSql());
        long count = 0;
        try {
            ResultSet rs = countPS.executeQuery();
            if (rs.next()) {
                count = rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataException("Unable to create prepared statements for CRUDRepository", e);
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
            throw new DataException("Unable to create prepared statements for CRUDRepository", e);
        }
    }

    public void deleteMultipleWithSimpleStatement (T ...entities){
        try {
            Statement statement = connection.createStatement();
            String ids = Arrays.stream(entities).map(T::getId).map(String::valueOf).collect(Collectors.joining(","));
            statement.executeUpdate(getDeleteMultipleWithSimpleStatement().replace(":ids", ids));
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataException("Unable to create prepared statements for CRUDRepository", e);
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
            throw new DataException("Unable to create prepared statements for CRUDRepository", e);
        }
    }


    abstract T mapForUpdate(PreparedStatement ps,T entity) throws SQLException;


    private String getSqlByAnnotation(String methodName){
        return Arrays.stream(this.getClass().getDeclaredMethods())
                .filter(m -> methodName.equals(m.getName()))
                .map(m -> m.getAnnotation(SQL.class))
                .map(SQL::value)
                .findFirst().orElse("");
    }

    abstract Entity extractEntityFromResultSet(ResultSet rs) throws SQLException;

}
