package com.sample.sampleapiservice.services;

import com.sample.sampleapiservice.domain.User;
import com.sample.sampleapiservice.exceptions.MkAuthException;

public interface IUserService {
    User validateUser(String email, String password) throws MkAuthException;

    User registerUser(String firstName, String lastName, String email, String password) throws MkAuthException;

}
