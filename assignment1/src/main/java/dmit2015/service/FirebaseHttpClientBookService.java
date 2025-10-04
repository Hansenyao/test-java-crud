package dmit2015.service;

import dmit2015.model.Book;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.JsonObject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class implements the BookService using the HttpClient library to send Http Request to the Firebase Realtime Database REST API.
 */

@Named("firebaseHttpClientBookService")
@ApplicationScoped
public class FirebaseHttpClientBookService implements BookService {
    /**
     * The base URL to the Firebase Realtime Database that is defined in `src/main/resources/META-INF/microprofile-config.properties` file.
     */
    @Inject
    @ConfigProperty(name = "firebase.rtdb.Book.base.url")
    private String firebaseRtdbBaseUrl;

    /**
     * The URL to the Firebase Realtime Database to access all data.
     */
    private String jsonAllDataPath;

    /**
     * HttpClient is native Java library for sending Http Request to a web server
     */
    private HttpClient httpClient;

    /**
     * Jsonb is used for converting Java objects to a JSON string or visa-versa
     */
    private Jsonb jsonb;

    @PostConstruct
    private void init() {
        httpClient = HttpClient.newHttpClient();
        jsonb = JsonbBuilder.create();
    }

    /**
     * Pushing currentBook data to Firebase Realtime Database using the REST API
     *
     * @link <a href="https://firebase.google.com/docs/reference/rest/database">Firebase Realtime Database REST API</a>
     */
    @Override
    public Book createBook(Book book) {
        // Build the url path to object to create
        jsonAllDataPath = String.format("%s/%s.json", firebaseRtdbBaseUrl, Book.class.getSimpleName());
        // Convert the Java object to a JSON string using JSONB
        String requestBodyJson = jsonb.toJson(book);

        // Create a Http Request for sending a Http POST request to push new data
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(jsonAllDataPath))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson, StandardCharsets.UTF_8))
                .build();
        try {
            // Send the Http Request
            var httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            // Check if the Http Request response is successful
            if (httpResponse.statusCode() == 200) {
                // Get the body of the Http Response
                var responseBodyJson = httpResponse.body();
                // Convert the JSON String to a JsonObject
                JsonObject responseJsonObject = jsonb.fromJson(responseBodyJson, JsonObject.class);
                // Set the unique key name for this object
                book.setId(responseJsonObject.getString("name"));

            } else {
                String errorMessage = String.format("Create was not successful with status code: %s", httpResponse.statusCode());
                throw new RuntimeException(errorMessage);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return book;
    }

    @Override
    public Optional<Book> getBookById(String id) {
        // Build the url path to object to update
        String jsonSingleDataPath = String.format("%s/%s/%s.json",
                firebaseRtdbBaseUrl, Book.class.getSimpleName(), id);
        try {
            // Create an GET Http Request to fetch all data
            var httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(jsonSingleDataPath))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();
            // Send the GET Http Request
            var httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            // Check if the Http Request was successful
            if (httpResponse.statusCode() == 200) {
                // Get the body of the Http Response
                var responseBodyJson = httpResponse.body();
                if (!responseBodyJson.equals("null")) {
                    // Convert the responseBodyJson to an LinkedHashMap<String, Book>
                    Book responseData = jsonb.fromJson(responseBodyJson, Book.class);
                    responseData.setId(id);
                    return Optional.of(responseData);
                } else {
                    throw new RuntimeException(String.format("Book with id of %s not found", id));
                }

            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public List<Book> getAllBooks() {
        // Build the url path to object to get all data
        jsonAllDataPath = String.format("%s/%s.json", firebaseRtdbBaseUrl, Book.class.getSimpleName());
        // Create an GET Http Request to fetch all data
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(jsonAllDataPath))
                .header("Content-Type", "application/json")
                .GET()
                .build();
        try {
            // Send the GET Http Request
            var httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            // Check if the Http Request was successful
            if (httpResponse.statusCode() == 200) {
                // Get the body of the Http Response
                var responseBodyJson = httpResponse.body();
                // Convert the responseBodyJson to an LinkedHashMap<String, Book>
                LinkedHashMap<String, Book> responseData = jsonb.fromJson(responseBodyJson, new LinkedHashMap<String, Book>() {
                }.getClass().getGenericSuperclass());
                if (responseData != null) {
                    // Convert the LinkedHashMap<String, Book> to List<Book>
                    return responseData.entrySet()
                            .stream()
                            .map(item -> {
                                var currentBook = new Book();
                                currentBook.setId(item.getKey());
                                currentBook.setTitle(item.getValue().getTitle());
                                currentBook.setAuthor(item.getValue().getAuthor());
                                currentBook.setPublishDate(item.getValue().getPublishDate());
                                currentBook.setReadAlready(item.getValue().isReadAlready());
                                return currentBook;
                            })
                            .toList();
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return List.of();
    }

    /**
     * Writing currentBook data to Firebase Realtime Database using the REST API
     *
     * @link <a href="https://firebase.google.com/docs/reference/rest/database">Firebase Realtime Database REST API</a>
     */
    @Override
    public Book updateBook(Book book) {
        // Build the url path to object to update
        String jsonSingleDataPath = String.format("%s/%s/%s.json",
                firebaseRtdbBaseUrl, Book.class.getSimpleName(), book.getId());

        // Convert the Java object to a JSON string using JSONB
        String requestBodyJson = jsonb.toJson(book);

        // Create and Http Request to send an HTTP PUT request to write over existing data
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(jsonSingleDataPath))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(requestBodyJson, StandardCharsets.UTF_8))
                .build();
        try {
            // Send the Http Request
            var httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            // Check if the Http Response was successful
            if (httpResponse.statusCode() != 200) {
                String errorMessage = String.format("Update was not successful with status code: %s", httpResponse.statusCode());
                throw new RuntimeException(errorMessage);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return book;
    }

    /**
     * Remove data from Firebase Realtime Database using the REST API
     *
     * @link <a href="https://firebase.google.com/docs/reference/rest/database">Firebase Realtime Database REST API</a>
     */
    @Override
    public void deleteBookById(String id) {
        // Build the URL path of the Json object to delete
        String jsonSingleDataPath = String.format("%s/%s/%s.json",
                firebaseRtdbBaseUrl, Book.class.getSimpleName(), id);
        // Create an DELETE Http Request
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(jsonSingleDataPath))
                .DELETE()
                .build();
        try {
            // Send the DELETE Http Request
            var httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            // Check if the Http Response was successful
            if (httpResponse.statusCode() != 200) {
                String errorMessage = String.format("Delete was not successful with status code: %s", httpResponse.statusCode());
                throw new RuntimeException(errorMessage);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}