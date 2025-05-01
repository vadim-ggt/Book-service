package ru.store.springbooks.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.store.springbooks.exception.*;
import ru.store.springbooks.model.Library;
import ru.store.springbooks.model.User;
import ru.store.springbooks.repository.UserRepository;
import ru.store.springbooks.service.UserService;
import ru.store.springbooks.utils.CustomCache;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomCache<Long, User> userCache;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setPassword("ValidPass123");
        testUser.setEmail("test@example.com");
    }

    @Test
    void testDeleteUserWithRepositoryException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        doThrow(new RuntimeException("DB error")).when(userRepository).deleteUserLibraryLinks(anyLong());

        assertThrows(RuntimeException.class, () -> userService.deleteUser(1L));
    }

    @Test
    void testSaveUserWithValidEdgePassword() {
        testUser.setPassword("A1b2C3d4"); // минимум 8 символов, буквы и цифры
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User savedUser = userService.saveUser(testUser);

        assertNotNull(savedUser);
        verify(userRepository).save(testUser);
    }




    @Test
    void testGetUserLibrariesWhenUserNotFound() {
        when(userCache.get(anyLong())).thenReturn(null);
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getUserLibraries(1L));
    }


    @Test
    void testFindAllUsers() {
        List<User> users = List.of(testUser);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.findAllUsers();

        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0));
    }



    @Test
    void testSaveUser() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(userCache).put(anyLong(), any(User.class)); // Исправлено для void метода

        User savedUser = userService.saveUser(testUser);

        assertEquals(testUser, savedUser); // Используйте assertEquals вместо assert
        verify(userRepository, times(1)).save(testUser);
        verify(userCache, times(1)).put(testUser.getId(), savedUser);
    }

    @Test
    void testSaveUserWithEmptyUsername() {
        testUser.setUsername(""); // Устанавливаем пустое имя пользователя

        // Проверяем, что выбрасывается исключение EmptyFieldException при пустом username
        EmptyFieldException exception = assertThrows(EmptyFieldException.class, () -> {
            userService.saveUser(testUser);
        });

        // Ожидаем фактическое сообщение исключения
        assertEquals("Поле 'user.username' не может быть пустым или null", exception.getMessage());
    }

    @Test
    void testSaveUserWithInvalidEmail() {
        when(userRepository.existsByEmail(any())).thenReturn(true);  // Имитация существующего email

        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.saveUser(testUser);  // Тестируем сохранение пользователя с существующим email
        });

        assertEquals("Пользователь с email '" + testUser.getEmail() + "' уже существует", exception.getMessage());
    }

    @Test
    void testGetUserByIdWhenCacheIsEmpty() {
        when(userCache.get(anyLong())).thenReturn(null);  // Кэш пуст
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));  // Найден в базе данных

        User user = userService.getUserById(1L);

        assertEquals(testUser, user);
        verify(userRepository, times(1)).findById(anyLong());
        verify(userCache, times(1)).put(testUser.getId(), testUser);
    }

    @Test
    void testDeleteUserWithInvalidId() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());  // Не найден в базе данных

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            userService.deleteUser(1L);
        });

        assertEquals("User with id 1 not found", exception.getMessage());
    }

    @Test
    void testUpdateUserWithInvalidEmail() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(any())).thenReturn(true);  // Имитация существующего email

        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.updateUser(1L, testUser);  // Тестируем обновление с существующим email
        });

        assertEquals("Пользователь с email '" + testUser.getEmail() + "' уже существует", exception.getMessage());
    }

    @Test
    void testGetUserLibrariesWhenLibrariesAreEmpty() {
        testUser.setLibraries(List.of());  // Пустой список библиотек
        when(userCache.get(anyLong())).thenReturn(testUser);

        List<Library> libs = userService.getUserLibraries(1L);

        assertNotNull(libs);
        assertTrue(libs.isEmpty());  // Проверяем, что список пуст
    }


    @Test
    void testSaveUserWithNullUsername() {
        testUser.setUsername(null);  // Тестируем null для username

        EmptyFieldException exception = assertThrows(EmptyFieldException.class, () -> {
            userService.saveUser(testUser);
        });

        assertEquals("Поле 'user.username' не может быть пустым или null", exception.getMessage());
    }

    @Test
    void testSaveUserWithPasswordTooShort() {
        testUser.setPassword("A1b"); // < 8 символов
        assertThrows(InvalidPasswordException.class, () -> {
            userService.saveUser(testUser);
        });
    }

    @Test
    void testSaveUserWithPasswordWithoutDigit() {
        testUser.setPassword("Abcdefgh"); // Без цифр
        assertThrows(InvalidPasswordException.class, () -> {
            userService.saveUser(testUser);
        });
    }




    @Test
    void testUpdateUserWithNullPassword() {
        testUser.setPassword(null);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(new User()));

        assertThrows(EmptyFieldException.class, () -> {
            userService.updateUser(1L, testUser);
        });
    }

    @Test
    void testSaveUserWithPasswordWithoutUppercase() {
        testUser.setPassword("abcde123"); // Без заглавных букв
        assertThrows(InvalidPasswordException.class, () -> {
            userService.saveUser(testUser);
        });
    }


    @Test
    void testSaveUserWithNullEmail() {
        testUser.setEmail(null);  // Тестируем null для email

        EmptyFieldException exception = assertThrows(EmptyFieldException.class, () -> {
            userService.saveUser(testUser);
        });

        assertEquals("Поле 'user.email' не может быть пустым или null", exception.getMessage());
    }

    @Test
    void testGetUserByIdWhenCacheIsNotEmpty() {
        when(userCache.get(anyLong())).thenReturn(testUser);  // Кэш не пуст

        User result = userService.getUserById(1L);

        assertEquals(testUser, result);
        verify(userRepository, times(0)).findById(anyLong());  // Репозиторий не должен быть вызван
    }

    @Test
    void testDeleteUserWithValidId() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).deleteById(anyLong());  // Имитация удаления
        doNothing().when(userCache).remove(anyLong());  // Удаляем из кэша

        boolean result = userService.deleteUser(1L);

        assertTrue(result);
        verify(userRepository, times(1)).deleteById(anyLong());
        verify(userCache, times(1)).remove(anyLong());
    }



    @Test
    void testSaveUserWithPasswordShorterThanMinLength() {
        testUser.setPassword("short");

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class, () -> {
            userService.saveUser(testUser);
        });

        assertNotNull(exception);
    }


    @Test
    void testSaveUserWithExistingEmail() {
        when(userRepository.existsByEmail(any())).thenReturn(true);

        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.saveUser(testUser);
        });

        // Ожидаемое сообщение, которое будет передано в исключение
        String expectedMessage = "Пользователь с email '" + testUser.getEmail() + "' уже существует";

        // Сравниваем сообщение исключения с ожидаемым
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void testSaveUserWithInvalidPassword() {
        testUser.setPassword("short");

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class, () -> {
            userService.saveUser(testUser);
        });

        assertNotNull(exception);
    }

    @Test
    void testUpdateUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(userCache).put(anyLong(), any(User.class)); // Исправлено для void метода

        User updatedUser = new User();
        updatedUser.setId(1L); // Устанавливаем id для сравнения
        updatedUser.setUsername("newUsername");
        updatedUser.setPassword("NewPass123");
        updatedUser.setEmail("new@example.com");

        User result = userService.updateUser(1L, updatedUser);

        assertEquals(updatedUser, result); // Теперь сравниваем полные объекты

        verify(userRepository, times(1)).save(updatedUser);
        verify(userCache, times(1)).put(updatedUser.getId(), result);
    }

    @Test
    void testUpdateUserWithEmptyUsername() {
        User updatedUser = new User();
        updatedUser.setUsername("");

        // Имитируем наличие пользователя в базе
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(new User()));

        // Проверяем, что выбрасывается исключение EmptyFieldException, если username пуст
        EmptyFieldException exception = assertThrows(EmptyFieldException.class, () -> {
            userService.updateUser(1L, updatedUser);
        });

        // Ожидаем реальное сообщение исключения
        assertEquals("Поле 'user.username' не может быть пустым или null", exception.getMessage());
    }

    @Test
    void testDeleteUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).deleteById(anyLong()); // Исправлено для void метода
        doNothing().when(userCache).remove(anyLong()); // Исправлено для void метода

        boolean result = userService.deleteUser(1L);

        assertTrue(result); // Используйте assertTrue
        verify(userRepository, times(1)).deleteById(1L);
        verify(userCache, times(1)).remove(1L);
    }

    @Test
    void testDeleteUserThatDoesNotExist() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            userService.deleteUser(1L);
        });

        // Исправляем ожидаемое сообщение в соответствии с тем, как оно формируется в сервисе
        assertEquals("User with id 1 not found", exception.getMessage());
    }

    @Test
    void testGetUserById() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userCache.get(anyLong())).thenReturn(null); // Симулируем, что пользователя нет в кеше

        User user = userService.getUserById(1L);

        assertEquals(testUser, user);
        verify(userRepository, times(1)).findById(1L);
        verify(userCache, times(1)).put(testUser.getId(), testUser);
    }

    @Test
    void testGetUserByIdFromCache() {
        when(userCache.get(anyLong())).thenReturn(testUser);

        User user = userService.getUserById(1L);

        assertEquals(testUser, user);
        verify(userRepository, times(0)).findById(anyLong()); // Проверяем, что репозиторий не был вызван
    }


    @Test
    void testSaveUserWithExistingUsername() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByUsername(any())).thenReturn(true);

        UsernameAlreadyExistsException exception = assertThrows(UsernameAlreadyExistsException.class, () -> {
            userService.saveUser(testUser);
        });

        assertEquals("Пользователь с именем '" + testUser.getUsername() + "' уже существует", exception.getMessage());
    }

    @Test
    void testUpdateUserWithExistingEmail() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(any())).thenReturn(true);

        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.updateUser(1L, testUser);
        });

        assertEquals("Пользователь с email '" + testUser.getEmail() + "' уже существует", exception.getMessage());
    }

    @Test
    void testUpdateUserWithExistingUsername() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByUsername(any())).thenReturn(true);

        UsernameAlreadyExistsException exception = assertThrows(UsernameAlreadyExistsException.class, () -> {
            userService.updateUser(1L, testUser);
        });

        assertEquals("Пользователь с именем '" + testUser.getUsername() + "' уже существует", exception.getMessage());
    }

    @Test
    void testUpdateUserWithInvalidPassword() {
        User updatedUser = new User();
        updatedUser.setUsername("newUser");
        updatedUser.setEmail("new@example.com");
        updatedUser.setPassword("short");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByUsername(any())).thenReturn(false);

        InvalidPasswordException exception = assertThrows(InvalidPasswordException.class, () -> {
            userService.updateUser(1L, updatedUser);
        });

        assertNotNull(exception);
    }

    @Test
    void testGetUserLibraries() {
        testUser.setLibraries(List.of()); // или какие-то мокнутые библиотеки
        when(userCache.get(anyLong())).thenReturn(testUser);

        var libs = userService.getUserLibraries(1L);

        assertNotNull(libs);
        assertEquals(0, libs.size());
    }

    @Test
    void testGetUserByIdWhenNotFound() {
        when(userCache.get(anyLong())).thenReturn(null);
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            userService.getUserById(1L);
        });

        // Исправляем ожидаемое сообщение в соответствии с тем, как оно формируется в сервисе
        assertEquals("User with id 1 not found", exception.getMessage());
    }


}
