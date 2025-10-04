package dmit2015.assignment01_fadekeshodeinde.service;

import dmit2015.assignment01_fadekeshodeinde.model.Book;

import java.util.List;
import java.util.Optional;

public interface BookService {
    void create(Book newBook);
    List<Book> findAll();
    Optional<Book> findById(String id);
    void update(String id, Book updatedBook);
    void delete(String id);
}
