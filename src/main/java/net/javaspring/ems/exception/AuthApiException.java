package net.javaspring.ems.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class AuthApiException extends RuntimeException{
    private HttpStatus status;
    private String message;

}
