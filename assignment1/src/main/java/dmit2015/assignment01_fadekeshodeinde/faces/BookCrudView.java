package dmit2015.assignment01_fadekeshodeinde.faces;

import dmit2015.assignment01_fadekeshodeinde.model.Book;
import dmit2015.assignment01_fadekeshodeinde.service.BookService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("bookCrudView")
@ViewScoped
public class BookCrudView implements Serializable {

    @Inject
    private BookService bookService;

    private List<Book> books;
    private Book selectedBook = new Book();

    @PostConstruct
    public void init() {
        books = new ArrayList<>(bookService.findAll());
    }

    public void create() {
        if (selectedBook.getId() == null || selectedBook.getId().isBlank()) {
            bookService.create(selectedBook);
        } else {
            bookService.update(selectedBook.getId(), selectedBook);
        }
        books = new ArrayList<>(bookService.findAll());
        selectedBook = new Book();
    }

    public void edit(Book b) {
        this.selectedBook = b;
    }

    public void delete(String id) {
        bookService.delete(id);
        books = new ArrayList<>(bookService.findAll());
    }


    public List<Book> getBooks() { return books; }
    public Book getSelectedBook() { return selectedBook; }
    public void setSelectedBook(Book selectedBook) { this.selectedBook = selectedBook; }
}
