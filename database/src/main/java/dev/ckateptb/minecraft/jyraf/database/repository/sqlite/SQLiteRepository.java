package dev.ckateptb.minecraft.jyraf.database.repository.sqlite;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.ckateptb.minecraft.jyraf.database.dialect.SQLDialect;
import dev.ckateptb.minecraft.jyraf.database.repository.AbstractRepository;
import dev.ckateptb.minecraft.jyraf.internal.commons.io.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.file.Path;

@RequiredArgsConstructor
public class SQLiteRepository<Entity, Id> extends AbstractRepository<Entity, Id> {
    protected final Path path;

    @Override
    @SneakyThrows
    protected HikariDataSource createDatasource() {
        Path path = this.path;
        FileUtils.forceMkdirParent(path.toFile());
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(SQLDialect.SQLITE.getDriver());
        config.setJdbcUrl(SQLDialect.SQLITE.getProtocol() + path);
        config.setConnectionTimeout(0);
        return new HikariDataSource(config);
    }
}
