package dev.ckateptb.minecraft.jyraf.database.repository;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.zaxxer.hikari.HikariDataSource;
import dev.ckateptb.minecraft.jyraf.database.repository.crud.CRUDRepository;
import lombok.SneakyThrows;
import org.bukkit.plugin.Plugin;

public abstract class AbstractRepository<Entity, Id> implements CRUDRepository<Entity, Id> {
    protected Dao<Entity, Id> dao;
    protected ConnectionSource connection;

    @Override
    public Dao<Entity, Id> dao() {
        return this.dao;
    }

    @Override
    @SneakyThrows
    public void connect(Plugin owner, Class<Entity> entityClass, Class<Id> idClass) {
        HikariDataSource datasource = this.createDatasource();
        this.connection = new DataSourceConnectionSource(datasource, datasource.getJdbcUrl());
        TableUtils.createTableIfNotExists(this.connection, entityClass);
        this.dao = DaoManager.createDao(this.connection, entityClass);
    }

    protected abstract HikariDataSource createDatasource();

    @Override
    public void close() throws Exception {
        this.connection.close();
    }
}
