package dev.ckateptb.minecraft.jyraf.example.database;

import dev.ckateptb.minecraft.jyraf.Jyraf;
import dev.ckateptb.minecraft.jyraf.container.annotation.Component;
import dev.ckateptb.minecraft.jyraf.container.annotation.PostConstruct;
import dev.ckateptb.minecraft.jyraf.example.config.ConfigExample;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BookWriter {
    private final BookRepository repository;
    private final Jyraf plugin;
    private final ConfigExample config;

    @PostConstruct
    public void init() {
        if (!this.config.getDebug()) return;
        Book book = new Book();
        book.setName(UUID.randomUUID().toString());
        book.setAuthor("CKATEPTb");
        this.repository.save(book).subscribe(value -> plugin.getLogger().severe(Thread.currentThread().toString() + value));
    }
}
