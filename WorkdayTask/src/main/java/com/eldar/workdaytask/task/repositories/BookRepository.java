package com.eldar.workdaytask.task.repositories;

import com.eldar.workdaytask.task.entities.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Book findByTitle(String title);

    Page<Book> findByTitleOrAuthor(String title, String author, Pageable pageable);

    void deleteByTitle(String title);
}
