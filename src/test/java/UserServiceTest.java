import org.example.factory.PasswordEncoder;
import org.example.factory.User;
import org.example.factory.UserRepository;
import org.example.factory.UserService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


public class UserServiceTest {

    private static final String PASSWORD = "password";
    private static final String INVALID_PASSWORD_MESSAGE = "invalid";
    private static final User ENABLED_USER = new User("user id", "hash", true);
    private static final User DISABLED_USER = new User("disabled user id", "disabled user password hash", false);

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @Before
    public void init() {
        userRepository = createUserRepository();
        passwordEncoder = createPasswordEncoder();
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    public void shouldBeValidForValidCredentials() {
        boolean userIsValid = userService.isValidUser(ENABLED_USER.getId(), PASSWORD);
        // Kiểm tra kết quả của hàm isValidUser()
        assertTrue(userIsValid);

        // kiểm tra với hàm findById của userRepository có phải truyền vào đối số là "user id"
        verify(userRepository).findById(ENABLED_USER.getId());

        // kiểm tra với hàm encode của passwordEncoder có phải truyền vào đối số là "password"
        verify(passwordEncoder).encode(PASSWORD);
    }

    @Test
    public void checkTimeCallMethod() {
        boolean userIsValid = userService.isValidUser(ENABLED_USER.getId(), PASSWORD);
        // Kiểm tra kết quả của hàm isValidUser()
        assertTrue(userIsValid);

        // kiểm tra số lầm gọi hàm findById của userRepository
        verify(userRepository, times(1)).findById(ENABLED_USER.getId());

        // kiểm tra số lầm gọi hàm encode của passwordEncoder
        verify(passwordEncoder,times(1)).encode(PASSWORD);
    }


    @Test
    public void shouldBeInvalidForInvalidId() {
        boolean userIsValid = userService.isValidUser("invalid id", PASSWORD);
        // Kiểm tra kết quả của hàm isValidUser()
        assertFalse(userIsValid);

        InOrder inOrder = inOrder(userRepository, passwordEncoder);
        inOrder.verify(userRepository).findById("invalid id");
        inOrder.verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    public void shouldBeInvalidForInvalidPassword() {
        boolean userIsValid = userService.isValidUser(ENABLED_USER.getId(), INVALID_PASSWORD_MESSAGE);
        // Kiểm tra kết quả của hàm isValidUser()
        assertFalse(userIsValid);

        ArgumentCaptor<String> passwordCaptor = ArgumentCaptor.forClass(String.class);

        verify(passwordEncoder).encode(passwordCaptor.capture());
        // Lấy ra giá trị đối số của phương thức encode()
        String value = passwordCaptor.getValue();
        // So sánh với đối số truyền vào từ đầu xem có đúng không
        assertEquals(INVALID_PASSWORD_MESSAGE, value);
    }

    @Test
    public void shouldBeInvalidForDisabledUser() {
        boolean userIsValid = userService.isValidUser(DISABLED_USER.getId(), PASSWORD);
        // Kiểm tra kết quả của hàm isValidUser()
        assertFalse(userIsValid);
       // Đảm bảo gọi hàm findById()
        verify(userRepository).findById(DISABLED_USER.getId());
        // Kiểm tra xem có phải hàm encode() của passwordEncoder đang không được gọi không
        verifyZeroInteractions(passwordEncoder);
    }


    private PasswordEncoder createPasswordEncoder() {
        PasswordEncoder mock = mock(PasswordEncoder.class);
        when(mock.encode(anyString())).thenReturn("any password hash");
        when(mock.encode(PASSWORD)).thenReturn(ENABLED_USER.getPasswordHash());
        return mock;
    }

    private UserRepository createUserRepository() {
        UserRepository mock = mock(UserRepository.class);
        when(mock.findById(ENABLED_USER.getId())).thenReturn(ENABLED_USER);
        when(mock.findById(DISABLED_USER.getId())).thenReturn(DISABLED_USER);
        return mock;
    }
}