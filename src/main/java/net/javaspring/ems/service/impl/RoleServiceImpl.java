package net.javaspring.ems.service.impl;

import lombok.AllArgsConstructor;
import net.javaspring.ems.dto.RoleDto;
import net.javaspring.ems.entity.Role;
import net.javaspring.ems.repository.RoleRepository;
import net.javaspring.ems.service.RoleService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RoleServiceImpl implements RoleService {

    RoleRepository roleRepository;
    ModelMapper modelMapper;

    @Override
    public RoleDto createRole(RoleDto roleDto) {

        Role role = modelMapper.map(roleDto, Role.class);
        Role savedRole = roleRepository.save(role);

        return modelMapper.map(savedRole,RoleDto.class);
    }

    @Override
    public List<RoleDto> getAllRoles() {
        List<Role> roleList = roleRepository.findAll();
        return roleList.stream().map(role -> modelMapper.map(role,RoleDto.class)).collect(Collectors.toList());
    }
}
