package dev.ckateptb.minecraft.jyraf.example.database;

import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.database.repository.sqlite.SQLiteRepository;

import java.util.UUID;

@Component
public class BookRepository extends SQLiteRepository<Book, UUID> {
    public BookRepository(Jyraf jyraf) {
        super(jyraf.getDataFolder().toPath().resolve("database.db"));
    }
}
