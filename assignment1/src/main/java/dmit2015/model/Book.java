package dmit2015.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import net.datafaker.Faker;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Book {
    private String id;

    @NotBlank
    private String title;

    @NotBlank
    private String author;

    @NotNull
    private LocalDate publishDate;

    private boolean readAlready = false;

    public Book(Book other) {
        this.id = other.id;
        this.title = other.title;
        this.author = other.author;
        this.publishDate = other.publishDate;
        this.readAlready = other.readAlready;
    }

    public static Book copyOf(Book other) {
        return new Book(other);
    }

    public static Book of(Faker faker) {
        var newBook = new Book();
        Instant instant = faker.timeAndDate().past(100, TimeUnit.DAYS);
        newBook.setId(UUID.randomUUID().toString());
        newBook.setTitle(faker.book().title());
        newBook.setAuthor(faker.book().author());
        newBook.setPublishDate(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate());
        return newBook;
    }
}
