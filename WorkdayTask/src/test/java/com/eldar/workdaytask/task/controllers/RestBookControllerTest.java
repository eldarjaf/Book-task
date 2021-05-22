package com.eldar.workdaytask.task.controllers;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eldar.workdaytask.task.entities.Book;
import com.eldar.workdaytask.task.repositories.BookRepository;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;

@RunWith(SpringRunner.class)
@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@WithMockUser(roles = "USER")
class RestBookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockBean
    private BookRepository bookRepository;

    private Book book1;
    private Book book2;
    private Book book3;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        book1 = Book.builder()
                .author("Eldar")
                .details("horror")
                .title("Quantum Physics")
                .build();

        book2 = Book.builder()
                .author("Jane")
                .details("easy")
                .title("Digital Systems")
                .build();

        book3 = Book.builder()
                .author("John")
                .details("hard")
                .title("C++ Programming")
                .build();

        PageRequest request = PageRequest.of(0, 1);
        Page<Book> bookList = new PageImpl<>(Arrays.asList(book1), request, 1);
        Mockito.when(bookRepository.findByTitleOrAuthor(any(String.class), any(String.class), any(Pageable.class))).thenReturn(bookList);
    }

    @Test
    void findAllBooks() throws Exception {
        Mockito.when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2, book3));

        MvcResult result = mockMvc.perform(get("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        Assert.assertTrue(result.getResponse().getContentAsString().contains("Eldar"));
        Assert.assertTrue(result.getResponse().getContentAsString().contains("Quantum Physics"));
    }

    @Test
    void findAllBooksTestRestTemplate() {
        Mockito.when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2, book3));

        ResponseEntity<String> result = testRestTemplate.withBasicAuth("user", "user")
                .getForEntity("/books", String.class);

        Assert.assertEquals("200 OK", result.getStatusCode().toString());
        Assert.assertTrue(result.getBody().contains("Eldar"));
    }

    @Test
    void findBookDetail() throws Exception {
        Mockito.when(bookRepository.findByTitle(any(String.class))).thenReturn(book2);

        MvcResult result = mockMvc.perform(get("/books/details/Digital Systems")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        Assert.assertTrue(result.getResponse().getContentAsString().contains("easy"));
    }

    @Test
    void findBookDetailReturnsErrorMessage() throws Exception {
        Mockito.when(bookRepository.findByTitle(any(String.class))).thenReturn(null);

        MvcResult result = mockMvc.perform(get("/books/details/Java")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        Assert.assertTrue(result.getResponse().getContentAsString().contains("Book title not found"));
    }

    @Test
    void createBooks() throws Exception {
        Mockito.when(bookRepository.findByTitle(any(String.class))).thenReturn(null);

        MvcResult result = mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"author\":\"Eldar\", \"title\":\"Quantum Physics\", \"details\":\"horror\" }," +
                        "{ \"author\":\"Jane\", \"title\":\"Digital systems\", \"details\":\"easy\"}, " +
                        "\"author\":\"John\", \"title\":\"C++ programming\", \"details\":\"hard\"")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn();

        assertEquals(201, result.getResponse().getStatus());
    }

    @Test
    void createBooksWithDuplicateTitleReturnsErrorMessage() throws Exception {
        Mockito.when(bookRepository.findByTitle(any(String.class))).thenReturn(new Book());

        MvcResult result = mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"author\":\"Eldar\", \"title\":\"Quantum Physics\", \"details\":\"horror\" }," +
                        "{ \"author\":\"Jane\", \"title\":\"Digital systems\", \"details\":\"easy\"}, " +
                        "\"author\":\"John\", \"title\":\"C++ programming\", \"details\":\"hard\"")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        Assert.assertTrue(result.getResponse().getContentAsString().contains("Book title already exists"));
    }

    @Test
    void createBooksWithEmptyTitleReturnsErrorMessage() throws Exception {
        Mockito.when(bookRepository.findByTitle(any(String.class))).thenReturn(null);

        MvcResult result = mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"author\":\"Eldar\", \"title\":\" \", \"details\":\"horror\" }," +
                        "{ \"author\":\"Jane\", \"title\":\"Digital systems\", \"details\":\"easy\"}, " +
                        "\"author\":\"John\", \"title\":\"C++ programming\", \"details\":\"hard\"")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        Assert.assertTrue(result.getResponse().getContentAsString().contains("Title is required"));
    }

    @Test
    void updateBooks() throws Exception {
        Mockito.when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2, book3));
        Mockito.when(bookRepository.findByTitle(any(String.class))).thenReturn(book2);

        mockMvc.perform(patch("/books/update/Digital Systems")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"author\":\"Pavel\", \"title\":\"ALPs management\", \"details\":\"interactive\" }")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        MvcResult resultGet = mockMvc.perform(get("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        Assert.assertTrue(resultGet.getResponse().getContentAsString().contains("Pavel"));
        Assert.assertTrue(resultGet.getResponse().getContentAsString().contains("ALPs management"));
    }

    @Test
    void updateBooksWithNonExistingBookReturnsErrorMessage() throws Exception {
        Mockito.when(bookRepository.findByTitle(any(String.class))).thenReturn(null);

        MvcResult result = mockMvc.perform(patch("/books/update/Gender Studies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ \"author\":\"Pavel\", \"title\":\"ALPs management\", \"details\":\"interactive\" }")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        Assert.assertTrue(result.getResponse().getContentAsString().contains("Book title not found, please enter correct title that exists"));
    }

    @Test
    void getAllBooksOnPage() throws Exception {
        Mockito.when(bookRepository.findByTitle(any(String.class))).thenReturn(book2);

        MvcResult resultGet = mockMvc.perform(get("/books/pages?page=0&size=2&sort=title&title=Quantum Physics&author=Eldar")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        Assert.assertTrue(resultGet.getResponse().getContentAsString().contains("Eldar"));
        Assert.assertTrue(resultGet.getResponse().getContentAsString().contains("Quantum Physics"));
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void deleteBook() throws Exception {
        Mockito.when(bookRepository.findByTitle(any(String.class))).thenReturn(book1);

        MvcResult result = mockMvc.perform(delete("/books/delete/Quantum Physics")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(200, result.getResponse().getStatus());
        verify(bookRepository, times(1)).deleteByTitle(book1.getTitle());
    }

    @Test
    void deleteBookWithUserRole() throws Exception {
        Mockito.when(bookRepository.findByTitle(any(String.class))).thenReturn(book1);

        MvcResult result = mockMvc.perform(delete("/books/delete/Quantum Physics")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andReturn();

        assertEquals(403, result.getResponse().getStatus());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    void deleteNotExistingBookReturnsErrorMessage() throws Exception {
        Mockito.when(bookRepository.findByTitle(any(String.class))).thenReturn(null);

        MvcResult result = mockMvc.perform(delete("/books/delete/C")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        Assert.assertTrue(result.getResponse().getContentAsString().contains("Book title not found, please enter correct title that exists"));
    }
}