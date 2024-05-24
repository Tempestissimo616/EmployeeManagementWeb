package net.javaspring.ems.controller;

import lombok.AllArgsConstructor;
import net.javaspring.ems.dto.RoleDto;
import net.javaspring.ems.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@AllArgsConstructor
@RestController
@RequestMapping("/api/role")
public class RoleController {

    RoleService roleService;

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping
    public ResponseEntity<RoleDto> createRole(RoleDto roleDto){
        RoleDto role = roleService.createRole(roleDto);
        return new ResponseEntity<>(role, HttpStatus.CREATED);
    }

    public ResponseEntity<List<RoleDto>> getAllRoles(){
        List<RoleDto> roleList = roleService.getAllRoles();
        return new ResponseEntity<>(roleList, HttpStatus.OK);
    }

}
