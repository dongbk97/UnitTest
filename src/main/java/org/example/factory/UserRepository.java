package org.example.factory;

public class UserRepository {
    public User findById(String id) {
        return new User();
    }
}
