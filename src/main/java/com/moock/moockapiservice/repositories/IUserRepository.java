package com.moock.moockapiservice.repositories;

import com.moock.moockapiservice.domain.User;
import com.moock.moockapiservice.exceptions.MkAuthException;

public interface IUserRepository {
    Integer create(String firstName, String lastName, String email, String password) throws MkAuthException;

    User findByEmailAndPassword(String email, String password) throws MkAuthException;

    Integer getCountByEmail(String email);

    User findById(Integer userId);

}
