package dev.ckateptb.minecraft.jyraf.database.repository.h2;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.ckateptb.minecraft.jyraf.database.dialect.SQLDialect;
import dev.ckateptb.minecraft.jyraf.database.repository.AbstractRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class H2Repository<Entity, Id> extends AbstractRepository<Entity, Id> {
    protected final String url;
    protected final String username;
    protected final String password;

    @Override
    protected HikariDataSource createDatasource() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(SQLDialect.H2.getDriver());
        hikariConfig.setJdbcUrl(SQLDialect.H2.getProtocol() + this.url);
        hikariConfig.setUsername(this.username);
        hikariConfig.setPassword(this.password);
        return new HikariDataSource(hikariConfig);
    }
}
