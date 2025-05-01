package ru.store.springbooks.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.store.springbooks.exception.EmptyFieldException;
import ru.store.springbooks.exception.EntityNotFoundException;
import ru.store.springbooks.model.Library;
import ru.store.springbooks.model.User;
import ru.store.springbooks.repository.LibraryRepository;
import ru.store.springbooks.repository.UserRepository;
import ru.store.springbooks.utils.CustomCache;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LibraryServiceImplTest {

    @InjectMocks
    private LibraryServiceImpl libraryService;

    @Mock
    private LibraryRepository libraryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomCache<Long, Library> libraryCache;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveLibrary_Valid() {
        Library lib = Library.builder().name("MyLib").address("Street 1").build();
        Library saved = Library.builder().id(1L).name("MyLib").address("Street 1").build();

        when(libraryRepository.save(lib)).thenReturn(saved);

        Library result = libraryService.saveLibrary(lib);
        assertEquals("MyLib", result.getName());
        verify(libraryCache).put(1L, saved);
    }

    @Test
    void testSaveLibrary_EmptyName() {
        Library lib = Library.builder().name("").address("Some").build();
        assertThrows(EmptyFieldException.class, () -> libraryService.saveLibrary(lib));
    }

    @Test
    void testSaveLibrary_EmptyAddress() {
        Library lib = Library.builder().name("Some").address(" ").build();
        assertThrows(EmptyFieldException.class, () -> libraryService.saveLibrary(lib));
    }

    @Test
    void testGetLibraryById_FromCache() {
        Library cached = Library.builder().id(1L).name("Lib").address("Addr").build();
        when(libraryCache.get(1L)).thenReturn(cached);

        Library result = libraryService.getLibraryById(1L);
        assertEquals("Lib", result.getName());
        verify(libraryRepository, never()).findById(1L);
    }

    @Test
    void testGetLibraryById_FromDb() {
        Library lib = Library.builder().id(1L).name("Lib").address("Addr").build();
        when(libraryCache.get(1L)).thenReturn(null);
        when(libraryRepository.findById(1L)).thenReturn(Optional.of(lib));

        Library result = libraryService.getLibraryById(1L);
        assertEquals("Lib", result.getName());
        verify(libraryCache).put(1L, lib);
    }

    @Test
    void testGetLibraryById_NotFound() {
        when(libraryCache.get(1L)).thenReturn(null);
        when(libraryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> libraryService.getLibraryById(1L));
    }

    @Test
    void testDeleteLibrary_Exists() {
        when(libraryRepository.existsById(1L)).thenReturn(true);
        boolean result = libraryService.deleteLibrary(1L);
        assertTrue(result);
        verify(libraryRepository).deleteById(1L);
        verify(libraryCache).remove(1L);
    }

    @Test
    void testDeleteLibrary_NotExists() {
        when(libraryRepository.existsById(1L)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> libraryService.deleteLibrary(1L));
    }

    @Test
    void testAddUserToLibrary() {
        // Используем ArrayList вместо HashSet
        Library lib = Library.builder().id(1L).name("Lib").users(new ArrayList<>()).build();
        User user = User.builder().id(2L).libraries(new ArrayList<>()).build(); // изменено на ArrayList

        when(libraryRepository.findById(1L)).thenReturn(Optional.of(lib));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(libraryRepository.save(lib)).thenReturn(lib);

        Library result = libraryService.addUserToLibrary(1L, 2L);
        assertTrue(result.getUsers().contains(user));
        verify(userRepository).save(user);
        verify(libraryCache).put(1L, lib);
    }

    @Test
    void testUpdateLibrary_Valid() {
        Library existing = Library.builder().id(1L).name("Old").address("Old").build();
        Library updated = Library.builder().name("New").address("New").build();

        when(libraryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(libraryRepository.save(any())).thenReturn(existing);

        Library result = libraryService.updateLibrary(1L, updated);
        assertEquals("New", result.getName());
        verify(libraryCache).put(1L, existing);
    }

    @Test
    void testUpdateLibrary_EmptyFields() {
        Library existing = Library.builder().id(1L).build();
        Library update = Library.builder().name("").address("").build(); // пустые поля

        when(libraryRepository.findById(1L)).thenReturn(Optional.of(existing));

        // Проверяем, что выбрасывается исключение EmptyFieldException
        assertThrows(EmptyFieldException.class, () -> libraryService.updateLibrary(1L, update));
    }



    @Test
    void testAddUserToLibrary_LibraryNotFound() {
        when(libraryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> libraryService.addUserToLibrary(1L, 2L));
    }

    @Test
    void testAddUserToLibrary_UserNotFound() {
        Library lib = Library.builder().id(1L).name("Lib").users(new ArrayList<>()).build();
        when(libraryRepository.findById(1L)).thenReturn(Optional.of(lib));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> libraryService.addUserToLibrary(1L, 2L));
    }

    @Test
    void testUpdateLibrary_NullNameAndAddress() {
        Library existing = Library.builder().id(1L).name("Old").address("Old").build();
        Library update = Library.builder().name(null).address(null).build(); // имя и адрес null

        when(libraryRepository.findById(1L)).thenReturn(Optional.of(existing));

        // Проверяем, что выбрасывается исключение
        assertThrows(EmptyFieldException.class, () -> libraryService.updateLibrary(1L, update));
    }

    @Test
    void testSaveLibrary_NullLibraryName() {
        Library lib = Library.builder().name(null).address("Some Address").build(); // null имя

        assertThrows(EmptyFieldException.class, () -> libraryService.saveLibrary(lib));
    }

    @Test
    void testDeleteLibrary_SuccessfulDeletion() {
        when(libraryRepository.existsById(1L)).thenReturn(true);
        boolean result = libraryService.deleteLibrary(1L);
        assertTrue(result);

        // Проверка, что библиотека была удалена
        verify(libraryRepository).deleteById(1L);
        verify(libraryCache).remove(1L);
    }

    @Test
    void testDeleteLibrary_CacheMiss() {
        // Тест на случай, когда библиотека не найдена в кэше
        Library lib = Library.builder().id(1L).name("Lib").address("Addr").build();
        when(libraryCache.get(1L)).thenReturn(null);
        when(libraryRepository.findById(1L)).thenReturn(Optional.of(lib));

        Library result = libraryService.getLibraryById(1L);

        assertEquals("Lib", result.getName());
        verify(libraryCache).put(1L, lib);
    }

    @Test
    void testSaveLibraryWithNullFields() {
        // Проверка на библиотеку с null-значениями
        Library lib = Library.builder().name(null).address("Some Address").build(); // имя null
        assertThrows(EmptyFieldException.class, () -> libraryService.saveLibrary(lib));
    }

    @Test
    void testGetLibraryById_CacheAndDbFallback() {
        // Сначала кеш пуст, но потом добавляем библиотеку в кэш
        Library lib = Library.builder().id(1L).name("Library").address("Street 123").build();
        when(libraryCache.get(1L)).thenReturn(null);  // кэш пуст
        when(libraryRepository.findById(1L)).thenReturn(Optional.of(lib)); // данные в БД

        Library result = libraryService.getLibraryById(1L);
        assertEquals("Library", result.getName());  // Проверка результата
        verify(libraryCache).put(1L, lib); // Библиотека должна быть добавлена в кэш
    }

    @Test
    void testAddUserToLibrary_WhenLibraryIsNull() {
        // Проверка, что если библиотека пустая, выбрасывается исключение
        when(libraryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> libraryService.addUserToLibrary(1L, 2L));
    }
}

