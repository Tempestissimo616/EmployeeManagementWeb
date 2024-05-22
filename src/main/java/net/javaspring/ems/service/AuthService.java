package net.javaspring.ems.service;

import net.javaspring.ems.dto.JwtAuthResponse;
import net.javaspring.ems.dto.LoginDto;
import net.javaspring.ems.dto.RegisterDto;

public interface AuthService {
    String register(RegisterDto registerDto);

    JwtAuthResponse login(LoginDto loginDto);
}
