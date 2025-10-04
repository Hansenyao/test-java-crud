package dmit2015.assignment01_fadekeshodeinde.service;

import dmit2015.assignment01_fadekeshodeinde.model.Book;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Alternative
@Priority(1)
@ApplicationScoped
public class FirebaseBookService implements BookService {

    @ConfigProperty(name = "firebase.rtdb.Book.base.url")
    String baseUrl;

    private final HttpClient http = HttpClient.newHttpClient();
    private final Jsonb jsonb = JsonbBuilder.create();

    private String booksPath() {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("Config 'firebase.rtdb.Book.base.url' is missing...");
        }
        return baseUrl.endsWith("/") ? baseUrl + "books" : baseUrl + "/books";
    }

    @Override
    public void create(Book newBook) {
        Book copy = new Book();
        copy.setTitle(newBook.getTitle());
        copy.setAuthor(newBook.getAuthor());
        copy.setPublishDate(newBook.getPublishDate());
        copy.setReadAlready(newBook.isReadAlready());

        String body = jsonb.toJson(copy);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(booksPath() + ".json"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        try {
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() >= 200 && res.statusCode() < 300) {
                Map<?, ?> map = jsonb.fromJson(res.body(), Map.class);
                Object fbId = map.get("name");
                if (fbId != null) newBook.setId(fbId.toString());
            } else {
                throw new RuntimeException("Create failed: " + res.statusCode() + " " + res.body());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Create failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Book> findAll() {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(booksPath() + ".json"))
                .timeout(Duration.ofSeconds(10))
                .GET().build();
        try {
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() == 200 && res.body() != null && !"null".equals(res.body())) {
                Map<String, Map<String, Object>> data =
                        jsonb.fromJson(res.body(), new HashMap<String, Map<String, Object>>(){}.getClass().getGenericSuperclass());
                return data.entrySet().stream().map(e -> {
                    Book b = jsonb.fromJson(jsonb.toJson(e.getValue()), Book.class);
                    b.setId(e.getKey());
                    return b;
                }).collect(Collectors.toList());
            }
            return new ArrayList<>();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("findAll failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Book> findById(String id) {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(booksPath() + "/" + id + ".json"))
                .timeout(Duration.ofSeconds(10))
                .GET().build();
        try {
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() == 200 && res.body() != null && !"null".equals(res.body())) {
                Book b = jsonb.fromJson(res.body(), Book.class);
                b.setId(id);
                return Optional.of(b);
            }
            return Optional.empty();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("findById failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(String id, Book updatedBook) {
        Book copy = new Book();
        copy.setTitle(updatedBook.getTitle());
        copy.setAuthor(updatedBook.getAuthor());
        copy.setPublishDate(updatedBook.getPublishDate());
        copy.setReadAlready(updatedBook.isReadAlready());

        String body = jsonb.toJson(copy);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(booksPath() + "/" + id + ".json"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();
        try {
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() < 200 || res.statusCode() >= 300) {
                throw new RuntimeException("Update failed: " + res.statusCode() + " " + res.body());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Update failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String id) {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(booksPath() + "/" + id + ".json"))
                .timeout(Duration.ofSeconds(10))
                .DELETE().build();
        try {
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() < 200 || res.statusCode() >= 300) {
                throw new RuntimeException("Delete failed: " + res.statusCode() + " " + res.body());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Delete failed: " + e.getMessage(), e);
        }
    }
}
