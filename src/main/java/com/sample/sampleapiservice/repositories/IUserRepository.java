package com.sample.sampleapiservice.repositories;

import com.sample.sampleapiservice.domain.User;
import com.sample.sampleapiservice.exceptions.MkAuthException;

public interface IUserRepository {
    Integer create(String firstName, String lastName, String email, String password) throws MkAuthException;

    User findByEmailAndPassword(String email, String password) throws MkAuthException;

    Integer getCountByEmail(String email);

    User findById(Integer userId);

}
