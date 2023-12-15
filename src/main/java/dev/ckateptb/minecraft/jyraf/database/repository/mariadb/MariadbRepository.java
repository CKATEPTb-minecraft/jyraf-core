package dev.ckateptb.minecraft.jyraf.database.repository.mariadb;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.ckateptb.minecraft.jyraf.database.dialect.SQLDialect;
import dev.ckateptb.minecraft.jyraf.database.repository.AbstractRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MariadbRepository<Entity, Id> extends AbstractRepository<Entity, Id> {
    protected final String url;
    protected final String username;
    protected final String password;

    @Override
    protected HikariDataSource createDatasource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(SQLDialect.MARIADB.getDriver());
        hikariConfig.setJdbcUrl(SQLDialect.MARIADB.getProtocol() + this.url);
        hikariConfig.setUsername(this.username);
        hikariConfig.setPassword(this.password);
        return new HikariDataSource(hikariConfig);
    }
}
