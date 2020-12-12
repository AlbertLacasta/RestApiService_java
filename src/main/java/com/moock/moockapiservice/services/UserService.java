package com.moock.moockapiservice.services;

import com.moock.moockapiservice.domain.User;
import com.moock.moockapiservice.exceptions.MkAuthException;
import com.moock.moockapiservice.repositories.IUserRepository;
import com.moock.moockapiservice.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@Transactional
public class UserService implements IUserService {

    @Autowired
    UserRepository userRepository;

    @Override
    public User validateUser(String email, String password) throws MkAuthException {
        if(email != null) {
            email.toLowerCase();
        }

        return userRepository.findByEmailAndPassword(email, password);
    }

    @Override
    public User registerUser(String firstName, String lastName, String email, String password) throws MkAuthException {
        Pattern pattern = Pattern.compile("^(.+)@(.+)$");
        if(email != null) {
            email.toLowerCase();
        }

        if(!pattern.matcher(email).matches()) {
            throw new MkAuthException("Invalid email");
        }

        Integer count = userRepository.getCountByEmail(email);

        if(count > 0) {
            throw new MkAuthException("Email already in use");
        }

        Integer userId = userRepository.create(firstName, lastName, email, password);
        return userRepository.findById(userId);
    }

}
