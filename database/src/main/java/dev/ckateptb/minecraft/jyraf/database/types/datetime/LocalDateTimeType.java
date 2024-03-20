package dev.ckateptb.minecraft.jyraf.database.types.datetime;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.LongType;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class LocalDateTimeType extends LongType {

    public LocalDateTimeType() {
        super(SqlType.LONG, new Class<?>[]{LocalDateTime.class});
    }

    @Override
    public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
        long value = (Long) super.sqlArgToJava(fieldType, sqlArg, columnPos);
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault());
    }

    @Override
    public Object javaToSqlArg(FieldType fieldType, Object object) {
        LocalDateTime localDateTime = (LocalDateTime) object;
        return ZonedDateTime.of(localDateTime, ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}