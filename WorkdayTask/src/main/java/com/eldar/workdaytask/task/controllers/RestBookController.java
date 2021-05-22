package com.eldar.workdaytask.task.controllers;

import com.eldar.workdaytask.task.config.DuplicateTitleException;
import com.eldar.workdaytask.task.config.NoResultFoundException;
import com.eldar.workdaytask.task.entities.Book;
import com.eldar.workdaytask.task.repositories.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Transactional
@RestController
@RequestMapping("/books")
public class RestBookController {

    @Autowired
    private BookRepository bookRepository;

    @GetMapping
    public ResponseEntity<List<Book>> findAllBooks() {
        log.debug("Inside findAllBooks()");
        return ResponseEntity.ok(bookRepository.findAll());
    }

    @GetMapping("/details/{title}")
    public ResponseEntity<String> findBookDetail(@PathVariable("title") String title) throws HttpServerErrorException.InternalServerError {
        Book result = bookRepository.findByTitle(title);
        log.debug("Inside findBookDetail() with title={}.", title);
        if (result == null) {
            throw new NoResultFoundException("Book title not found, please enter correct title that exists");
        }
        return ResponseEntity.ok(result.getDetails());

    }

    @PostMapping
    public ResponseEntity<Void> createBooks(@Valid @RequestBody Book book) {
        log.debug("Inside createBooks() with title={}., author={}.,details={}.", book.getTitle(), book.getAuthor(), book.getDetails());
        Book book1 = bookRepository.findByTitle(book.getTitle());
        if (book1 != null) {
            throw new DuplicateTitleException("Book title already exists");
        }
        bookRepository.save(book);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/update/{title}")
    public ResponseEntity<Book> updateBooks(@PathVariable("title") String title, @Valid @RequestBody Book updatedBook) {
        log.debug("Inside updateBooks() with title={}., author={}.,details={}.", updatedBook.getTitle(), updatedBook.getAuthor(), updatedBook.getDetails());
        Book book = bookRepository.findByTitle(title);
        if (book == null) {
            throw new NoResultFoundException("Book title not found, please enter correct title that exists");
        }
        book.setAuthor(updatedBook.getAuthor());
        book.setTitle(updatedBook.getTitle());
        bookRepository.save(book);
        return ResponseEntity.ok(book);
    }

//    @GetMapping("/pages")
//    public ResponseEntity<List<Book>> getAllBooksOnPage(@RequestParam("pageNo") int pageNo,
//                                                        @RequestParam("pageSize") int pageSize,
//                                                        @RequestParam("sortBy") String sortBy) {
//        Pageable paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
//        Page<Book> pagedResult = bookRepository.findAll(paging);
//
//        return ResponseEntity.ok(pagedResult.getContent());
//    }

    @GetMapping("/pages")
    public ResponseEntity<List<Book>> getAllBooksOnPage(@PageableDefault(page = 0, size = 3)
                                                        @SortDefault(sort = "id", direction = Sort.Direction.ASC)
                                                                Pageable pageable,
                                                        @RequestParam(value = "title", required = false) String title,
                                                        @RequestParam(value = "author", required = false) String author) {
        log.debug("Inside getAllBooksOnPage() with page={}., size={}.,sort={}., title={}., author={}.,", pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort(), title, author);
        Page<Book> pages = bookRepository.findByTitleOrAuthor(title, author, pageable);
        return ResponseEntity.ok(pages.getContent());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{title}")
    public ResponseEntity<Void> deleteBook(@Valid @PathVariable String title) {
        log.debug("Inside deleteBooks() with title={}.", title);
        Book book = bookRepository.findByTitle(title);
        if (book == null) {
            throw new NoResultFoundException("Book title not found, please enter correct title that exists");
        }
        bookRepository.deleteByTitle(title);

        return ResponseEntity.status(HttpStatus.OK).build();
    }


}
