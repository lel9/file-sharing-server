package service;

import exception.AppException;
import model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.UserRepository;

@Service("userService")
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User registerUser(String username, String email, String password) throws AppException {
        if (email.isEmpty())
            throw new AppException("Email can not be empty!");
        if (username.isEmpty())
            throw new AppException("Username can not be empty!");
        if (password.isEmpty())
            throw new AppException("Password can not be empty!");

        User user = userRepository.findByUsername(username);
        if (user != null) {
            throw new AppException("Username already exists!");
        }

        User newUser = new User(username, email, password);
        userRepository.save(newUser);
        return newUser;
    }

    public User authorizeUser(String username, String password) throws AppException {
        if (username.isEmpty())
            throw new AppException("Username can not be empty!");
        if (password.isEmpty())
            throw new AppException("Password can not be empty!");

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new AppException("Ошибка авторизации");
        }
        if (!user.getPassword().equals(password)) {
            throw new AppException("Ошибка авторизации");
        }
        return user;
    }
}
