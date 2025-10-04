package dmit2015.assignment01_fadekeshodeinde.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class Book {
    private String id;


    @NotBlank
    private String title;

    @NotBlank
    private String author;

    @NotNull
    private LocalDate publishDate;

    private boolean readAlready;
}
