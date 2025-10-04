package dmit2015.service;

import dmit2015.model.Book;

import java.util.List;
import java.util.Optional;

public interface BookService {

    Book createBook(Book book);

    Optional<Book> getBookById(String id);

    List<Book> getAllBooks();

    Book updateBook(Book book);

    void deleteBookById(String id);
}