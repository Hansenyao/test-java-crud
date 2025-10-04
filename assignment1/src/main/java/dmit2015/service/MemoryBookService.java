package dmit2015.service;

import dmit2015.model.Book;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import net.datafaker.Faker;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.random.RandomGenerator;

@Named("memoryBookService")
@ApplicationScoped
public class MemoryBookService implements BookService {

    private final List<Book> books = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void init() {

        var faker = new Faker();
        for (int counter = 1; counter <= 5; counter++) {
            var currentBook = Book.of(faker);
            books.add(currentBook);
        }

    }

    @Override
    public Book createBook(Book book) {
        Objects.requireNonNull(book, "Book to create must not be null");

        // Assign a fresh id on create to ensure uniqueness (ignore any incoming id)
        Book stored = Book.copyOf(book);
        stored.setId(UUID.randomUUID().toString());
        books.add(stored);

        // Return a defensive copy
        return Book.copyOf(stored);
    }

    @Override
    public Optional<Book> getBookById(String id) {
        Objects.requireNonNull(id, "id must not be null");

        return books.stream()
                .filter(currentBook -> currentBook.getId().equals(id))
                .findFirst()
                .map(Book::copyOf); // return a copy to avoid external mutation

    }

    @Override
    public List<Book> getAllBooks() {
        // Unmodifiable snapshot of copies
        return books.stream().map(Book::copyOf).toList();
    }

    @Override
    public Book updateBook(Book book) {
        Objects.requireNonNull(book, "Book to update must not be null");
        Objects.requireNonNull(book.getId(), "Book id must not be null");

        // Find index of existing task by id
        int index = -1;
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getId().equals(book.getId())) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            throw new NoSuchElementException("Could not find Task with id: " + book.getId());
        }

        // Replace stored item with a copy (preserve id)
        Book stored = Book.copyOf(book);
        books.set(index, stored);

        return Book.copyOf(stored);
    }

    @Override
    public void deleteBookById(String id) {
        Objects.requireNonNull(id, "id must not be null");

        boolean removed = books.removeIf(currentBook -> id.equals(currentBook.getId()));
        if (!removed) {
            throw new NoSuchElementException("Could not find Task with id: " + id);
        }
    }
}
