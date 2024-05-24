package net.javaspring.ems.service;

import net.javaspring.ems.dto.RoleDto;

import java.util.List;

public interface RoleService {

    RoleDto createRole(RoleDto roleDto);

    List<RoleDto> getAllRoles();
}
