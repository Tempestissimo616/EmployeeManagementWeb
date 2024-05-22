package net.javaspring.ems.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
public class UserPermissionNotAllowedException extends RuntimeException{
    public UserPermissionNotAllowedException(){
        super("User does not have permission");
    }
}
