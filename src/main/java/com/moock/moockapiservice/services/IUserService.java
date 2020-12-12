package com.moock.moockapiservice.services;

import com.moock.moockapiservice.domain.User;
import com.moock.moockapiservice.exceptions.MkAuthException;

public interface IUserService {
    User validateUser(String email, String password) throws MkAuthException;

    User registerUser(String firstName, String lastName, String email, String password) throws MkAuthException;

}
