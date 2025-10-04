package dmit2015.assignment01_fadekeshodeinde.service;

import dmit2015.assignment01_fadekeshodeinde.model.Book;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class MemoryBookService implements BookService {

    private final Map<String, Book> store = new ConcurrentHashMap<>();

    public MemoryBookService() {
        // seed 3
        create(sample("Clean Code","Robert C. Martin", LocalDate.of(2008,8,1), true));
        create(sample("Effective Java","Joshua Bloch", LocalDate.of(2018,1,6), false));
        create(sample("Pragmatic Programmer","Hunt & Thomas", LocalDate.of(1999,10,30), true));
    }

    private static Book sample(String title, String author, LocalDate date, boolean read) {
        Book b = new Book();
        b.setTitle(title); b.setAuthor(author); b.setPublishDate(date); b.setReadAlready(read);
        return b;
    }

    @Override public void create(Book newBook) {
        if (newBook.getId() == null || newBook.getId().isBlank()) {
            newBook.setId(UUID.randomUUID().toString());
        }
        store.put(newBook.getId(), newBook);
    }

    @Override public List<Book> findAll() { return new ArrayList<>(store.values()); }

    @Override public Optional<Book> findById(String id) { return Optional.ofNullable(store.get(id)); }

    @Override public void update(String id, Book updatedBook) {
        updatedBook.setId(id);
        store.put(id, updatedBook);
    }

    @Override public void delete(String id) { store.remove(id); }
}
