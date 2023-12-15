package dev.ckateptb.minecraft.jyraf.database.dialect;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SQLDialect {
    H2("org.h2.Driver", "jdbc:h2:"),
    SQLITE("org.sqlite.JDBC", "jdbc:sqlite:"),
    MYSQL("com.mysql.cj.jdbc.Driver", "jdbc:mysql://"),
    MARIADB("org.mariadb.jdbc.Driver", "jdbc:mariadb://"),
    POSTGRESQL("org.postgresql.Driver", "jdbc:postgresql://");

    private final String driver;
    private final String protocol;
}
