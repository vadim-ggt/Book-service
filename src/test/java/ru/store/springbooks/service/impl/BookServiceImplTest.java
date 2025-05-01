package ru.store.springbooks.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.store.springbooks.exception.EmptyFieldException;
import ru.store.springbooks.exception.EntityNotFoundException;
import ru.store.springbooks.exception.InvalidAuthorNameException;
import ru.store.springbooks.model.Book;
import ru.store.springbooks.model.Library;
import ru.store.springbooks.repository.BookRepository;
import ru.store.springbooks.repository.LibraryRepository;
import ru.store.springbooks.utils.CustomCache;

class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LibraryRepository libraryRepository;

    @Mock
    private CustomCache<Long, Book> bookCache;

    @InjectMocks
    private BookServiceImpl bookService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }


    @Test
    void saveBooksBulk_shouldThrow_whenAuthorIsNull() {
        Library lib = new Library();
        lib.setId(1L);
        Book book = new Book();
        book.setTitle("Valid");
        book.setAuthor(null);
        book.setLibrary(lib);

        assertThrows(EmptyFieldException.class, () -> bookService.saveBooksBulk(List.of(book)));
    }


    @Test
    void saveBooksBulk_shouldThrow_whenAuthorContainsDigits() {
        Library lib = new Library();
        lib.setId(1L);
        Book book = new Book();
        book.setTitle("Valid");
        book.setAuthor("John123");
        book.setLibrary(lib);

        assertThrows(InvalidAuthorNameException.class, () -> bookService.saveBooksBulk(List.of(book)));
    }


    @Test
    void saveBooksBulk_shouldThrow_whenLibraryIdIsNull() {
        Book book = new Book();
        book.setTitle("Valid");
        book.setAuthor("Author");
        book.setLibrary(new Library()); // id == null

        assertThrows(EmptyFieldException.class, () -> bookService.saveBooksBulk(List.of(book)));
    }


    @Test
    void saveBooksBulk_shouldThrow_whenLibraryNotFound() {
        Library lib = new Library();
        lib.setId(404L);

        Book book = new Book();
        book.setTitle("Valid");
        book.setAuthor("Author");
        book.setLibrary(lib);

        when(libraryRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookService.saveBooksBulk(List.of(book)));
    }


    @Test
    void findAllBooks_shouldReturnEmptyList_whenNoBooksFound() {
        when(bookRepository.findAll()).thenReturn(List.of());

        List<Book> result = bookService.findAllBooks();
        assertTrue(result.isEmpty());
    }

    @Test
    void findAllBooks_shouldLog_whenNoBooksFound() {
        when(bookRepository.findAll()).thenReturn(List.of());

        List<Book> result = bookService.findAllBooks();

        assertTrue(result.isEmpty());
    }


    @Test
    void saveBooksBulk_shouldSaveAllBooks_whenValid() {
        Library lib = new Library();
        lib.setId(1L);
        Book validBook = new Book();
        validBook.setTitle("Valid Book");
        validBook.setAuthor("Valid Author");
        validBook.setLibrary(lib);

        when(libraryRepository.findById(1L)).thenReturn(Optional.of(lib));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<Book> result = bookService.saveBooksBulk(List.of(validBook));
        assertEquals(1, result.size());
    }


    @Test
    void updateBook_shouldUpdateOnlySpecifiedFields() {
        Book existingBook = new Book();
        existingBook.setId(1L);
        existingBook.setTitle("Old Title");
        existingBook.setAuthor("Old Author");
        existingBook.setYear(1999);

        Book updatedBook = new Book();
        updatedBook.setAuthor("New Author");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenReturn(existingBook);

        Book result = bookService.updateBook(1L, updatedBook);
        assertEquals("New Author", result.getAuthor());
    }


    @Test
    void deleteBook_shouldRemoveFromCacheAndReturnTrue_whenBookIsFound() {
        Book book = new Book();
        book.setId(1L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        boolean result = bookService.deleteBook(1L);
        assertTrue(result);
        verify(bookCache).remove(1L);  // проверка, что книга удалена из кеша
        verify(bookRepository).deleteById(1L);  // проверка, что книга удалена из базы данных
    }

    @Test
    void searchBook_shouldReturnBooksByTitleAndAuthor() {
        Book book = new Book();
        book.setTitle("Test Book");
        book.setAuthor("Test Author");

        when(bookRepository.findByTitleIgnoreCaseAndAuthorIgnoreCase("Test Book", "Test Author"))
                .thenReturn(List.of(book));

        Map<String, String> params = new HashMap<>();
        params.put("title", "Test Book");
        params.put("author", "Test Author");

        List<Book> result = bookService.searchBook(params);
        assertEquals(1, result.size());
        assertEquals("Test Book", result.get(0).getTitle());
    }

    @Test
    void getBookById_shouldReturnBookFromCache() {
        Book cachedBook = new Book();
        cachedBook.setId(1L);
        cachedBook.setTitle("Cached Book");

        when(bookCache.get(1L)).thenReturn(cachedBook);

        Book result = bookService.getBookById(1L);
        assertEquals("Cached Book", result.getTitle());
        verify(bookRepository, never()).findById(1L);  // проверка, что не был вызван репозиторий
    }

    @Test
    void getBookById_shouldReturnBookFromDatabase_whenNotInCache() {
        Book dbBook = new Book();
        dbBook.setId(2L);
        dbBook.setTitle("DB Book");

        when(bookCache.get(2L)).thenReturn(null);
        when(bookRepository.findById(2L)).thenReturn(Optional.of(dbBook));

        Book result = bookService.getBookById(2L);
        assertEquals("DB Book", result.getTitle());
        verify(bookCache).put(2L, dbBook);  // проверка, что книга добавлена в кеш
    }


    @Test
    void saveBook_shouldThrowException_whenAuthorIsNull() {
        Book book = new Book();
        book.setTitle("Some Title");
        book.setLibrary(Library.builder()
                .id(1L)
                .name("Test Library")
                .build());

        assertThrows(EmptyFieldException.class, () -> bookService.saveBook(book));
    }

    @Test
    void saveBook_shouldThrowException_whenAuthorContainsDigits() {
        Book book = new Book();
        book.setAuthor("John123");
        book.setTitle("Test");
        book.setLibrary(Library.builder()
                .id(1L)
                .name("Test Library")
                .build());

        assertThrows(InvalidAuthorNameException.class, () -> bookService.saveBook(book));
    }

    @Test
    void saveBook_shouldThrowException_whenLibraryIdIsMissing() {
        Book book = new Book();
        book.setAuthor("John");
        book.setTitle("Test");
        book.setLibrary(new Library()); // id == null

        assertThrows(EmptyFieldException.class, () -> bookService.saveBook(book));
    }

    @Test
    void getBookById_shouldReturnFromCache() {
        Book cachedBook = new Book();
        cachedBook.setId(1L);
        when(bookCache.get(1L)).thenReturn(cachedBook);

        Book result = bookService.getBookById(1L);
        assertEquals(cachedBook, result);
    }

    @Test
    void getBookById_shouldThrow_whenBookNotFound() {
        when(bookCache.get(1L)).thenReturn(null);
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookService.getBookById(1L));
    }

    @Test
    void searchBook_shouldThrow_whenNoMatches() {
        when(bookRepository.findAll()).thenReturn(List.of());

        Map<String, String> params = new HashMap<>();
        List<Book> result = null;
        assertThrows(EntityNotFoundException.class, () -> bookService.searchBook(params));
    }

    @Test
    void deleteBook_shouldThrow_whenBookNotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookService.deleteBook(1L));
    }






    @Test
    void testGetBookById_NotFound() {
        when(bookCache.get(99L)).thenReturn(null);
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> bookService.getBookById(99L));
    }

    @Test
    void testSaveBook_Success() {
        Library library = new Library();
        library.setId(1L);

        Book book = new Book();
        book.setTitle("Test");
        book.setAuthor("Author");
        book.setLibrary(library);

        when(libraryRepository.findById(1L)).thenReturn(Optional.of(library));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> {
            Book b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });

        Book saved = bookService.saveBook(book);

        assertEquals("Test", saved.getTitle());
        verify(bookCache).put(eq(1L), any(Book.class));
    }

    @Test
    void updateBook_shouldUpdateCacheWhenBookIsUpdated() {
        Book oldBook = new Book();
        oldBook.setId(1L);
        oldBook.setTitle("OldTitle");
        oldBook.setAuthor("OldAuthor");

        Book updatedBook = new Book();
        updatedBook.setTitle("NewTitle");
        updatedBook.setAuthor("NewAuthor");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(oldBook));
        when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);

        Book result = bookService.updateBook(1L, updatedBook);
        assertEquals("NewTitle", result.getTitle());
        assertEquals("NewAuthor", result.getAuthor());
        verify(bookCache).put(1L, result);
    }

    @Test
    void searchBook_shouldReturnBooksByYear() {
        Book book = new Book();
        book.setTitle("TestBook");
        book.setYear(2020);

        when(bookRepository.findByYear(2020)).thenReturn(List.of(book));

        Map<String, String> params = new HashMap<>();
        params.put("year", "2020");

        List<Book> result = bookService.searchBook(params);
        assertEquals(1, result.size());
        assertEquals(2020, result.get(0).getYear());
    }







    @Test
    void deleteBook_shouldRemoveBookFromCacheAndDatabase() {
        Book book = new Book();
        book.setId(1L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        boolean result = bookService.deleteBook(1L);
        assertTrue(result);
        verify(bookCache).remove(1L);
        verify(bookRepository).deleteById(1L);
    }

    @Test
    void testGetBookById_ShouldUpdateCache() {
        Book dbBook = new Book();
        dbBook.setId(1L);
        dbBook.setTitle("DB");

        when(bookCache.get(1L)).thenReturn(null);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(dbBook));

        Book result = bookService.getBookById(1L);
        assertEquals("DB", result.getTitle());
        verify(bookCache).put(1L, dbBook);
    }




    @Test
    void testSaveBook_EmptyAuthor() {
        Book book = new Book();
        book.setAuthor(null);
        book.setTitle("Title");
        Library library = Library.builder()
                .id(1L)
                .name("Lib")
                .build();
        assertThrows(EmptyFieldException.class, () -> bookService.saveBook(book));
    }

    @Test
    void testSaveBook_InvalidAuthorName() {
        Library library = Library.builder()
                .id(1L)
                .name("Lib")
                .build();

        Book book = new Book();
        book.setAuthor("Author123"); // ← здесь ошибка
        book.setTitle("Title");
        book.setLibrary(library); // ← обязательно

        when(libraryRepository.findById(1L)).thenReturn(Optional.of(library));

        assertThrows(InvalidAuthorNameException.class, () -> bookService.saveBook(book));
    }

    @Test
    void testGetBookById_CacheHit() {
        Book cachedBook = new Book();
        cachedBook.setId(1L);
        cachedBook.setTitle("Cached");

        when(bookCache.get(1L)).thenReturn(cachedBook);

        Book result = bookService.getBookById(1L);
        assertEquals("Cached", result.getTitle());
        verify(bookRepository, never()).findById(any());
    }

    @Test
    void testGetBookById_CacheMiss() {
        Book dbBook = new Book();
        dbBook.setId(2L);
        dbBook.setTitle("DB");

        when(bookCache.get(2L)).thenReturn(null);
        when(bookRepository.findById(2L)).thenReturn(Optional.of(dbBook));

        Book result = bookService.getBookById(2L);
        assertEquals("DB", result.getTitle());
        verify(bookCache).put(2L, dbBook);
    }

    @Test
    void testDeleteBook() {
        Book book = new Book();
        book.setId(1L);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        boolean result = bookService.deleteBook(1L);
        assertTrue(result);
        verify(bookCache).remove(1L);
        verify(bookRepository).deleteById(1L);
    }

    @Test
    void testUpdateBook() {
        Book oldBook = Book.builder()
                .id(1L)
                .title("Old")
                .author("OldAuthor")
                .year(1990)
                .library(null)
                .build();
        Book updated = new Book();
        updated.setTitle("New");
        updated.setAuthor("NewAuthor");
        updated.setYear(2023);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(oldBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookService.updateBook(1L, updated);
        assertEquals("New", result.getTitle());
        assertEquals("NewAuthor", result.getAuthor());
        assertEquals(2023, result.getYear());
        verify(bookCache).put(1L, result);
    }

    @Test
    void testSearchBook_ByTitle() {
        Book oldBook = Book.builder()
                .id(1L)
                .title("Java")
                .author("Smith")
                .year(2020)
                .library(null)
                .build();

        when(bookRepository.findByTitleIgnoreCase("Java")).thenReturn(List.of(oldBook));

        Map<String, String> params = Map.of("title", "Java");
        List<Book> result = bookService.searchBook(params);

        assertEquals(1, result.size());
        assertEquals("Java", result.get(0).getTitle());
    }

    @Test
    void testSearchBook_NotFound() {
        when(bookRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(EntityNotFoundException.class, () -> bookService.searchBook(Collections.emptyMap()));
    }

    @Test
    void testSaveBooksBulk_ValidList() {
        Library lib = new Library();
        lib.setId(10L);

        Book book1 = Book.builder()
                .id(1L)
                .title("Title1")
                .author("Author")
                .year(2000)
                .library(lib)
                .build();
        Book book2 = Book.builder()
                .id(1L)
                .title("Title2")
                .author("Authorу")
                .year(2021)
                .library(lib)
                .build();
        when(libraryRepository.findById(10L)).thenReturn(Optional.of(lib));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> {
            Book b = inv.getArgument(0);
            b.setId(new Random().nextLong());
            return b;
        });

        List<Book> result = bookService.saveBooksBulk(List.of(book1, book2));

        assertEquals(2, result.size());
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }
}