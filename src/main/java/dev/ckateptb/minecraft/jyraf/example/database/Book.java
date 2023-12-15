package dev.ckateptb.minecraft.jyraf.example.database;


import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;

import java.util.UUID;

@Data
@DatabaseTable(tableName = "books")
public class Book {
    @DatabaseField(generatedId = true, canBeNull = false, dataType = DataType.UUID)
    private UUID id;
    @DatabaseField
    private String name;
    @DatabaseField
    private String author;
}
