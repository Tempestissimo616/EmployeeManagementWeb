package net.javaspring.ems.service.impl;

import lombok.AllArgsConstructor;
import net.javaspring.ems.dto.AuditApproveDto;
import net.javaspring.ems.dto.AuditDto;
import net.javaspring.ems.entity.Audit;
import net.javaspring.ems.entity.AuditApprove;
import net.javaspring.ems.entity.User;
import net.javaspring.ems.exception.ResourceNotFoundException;
import net.javaspring.ems.exception.UserPermissionNotAllowedException;
import net.javaspring.ems.repository.AuditApproveRepository;
import net.javaspring.ems.repository.AuditRepository;
import net.javaspring.ems.repository.UserRepository;
import net.javaspring.ems.service.AuditService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 审查管理 服务实现
 *
 * @author Phoenix
 */

@Service
@AllArgsConstructor
public class AuditServiceImpl implements AuditService {

    private AuditRepository auditRepository;

    private AuditApproveRepository auditApproveRepository;

    private UserRepository userRepository;

    private ModelMapper modelMapper;


    /**
     * 创建审查表
     *
     * @param auditDto 需要审查的信息
     * @return 创建表的信息
     */

    @Override
    public AuditDto createAudit(AuditDto auditDto) {

        Audit audit = modelMapper.map(auditDto,Audit.class);
        audit.setStatus(0);
        audit.setApprovals(new ArrayList<AuditApprove>());
        audit.setCreateTime(LocalDateTime.now());
        Audit savedAudit = auditRepository.save(audit);
        return modelMapper.map(savedAudit, AuditDto.class);
    }

    /**
     * 查询所有审查当前表的记录
     *
     * @param auditId 单一审查表ID
     * @return 查询结果
     */

    @Override
    public List<AuditApprove> getAllApprovalsById(Long auditId) {

        Audit audit = auditRepository.findById(auditId).orElseThrow(() -> new ResourceNotFoundException("Audit","id",auditId));
        return audit.getApprovals();
    }

    /**
     * 查询单一表的信息
     *
     * @param auditId 此表的ID
     * @return 查询结果
     */

    @Override
    public AuditDto getAuditById(Long auditId) {

        Audit audit = auditRepository.findById(auditId).orElseThrow(() -> new ResourceNotFoundException("Audit","id",auditId));
        return null;
    }

    /**
     * 查询所有审查的记录
     *
     * @return 查询结果
     */
    @Override
    public List<AuditDto> getAllAudit() {

        List<Audit> auditList = auditRepository.findAll();
        return auditList.stream().map(audit -> modelMapper.map(audit, AuditDto.class)).collect(Collectors.toList());
    }

    /**
     * 用户批准 批准逻辑
     *
     * @param auditApproveDto
     * @return 当前审查状态
     */

    @Override
    public AuditDto userApproveAudit(AuditApproveDto auditApproveDto) {

        Long userId = auditApproveDto.getUserId();
        Long auditId = auditApproveDto.getAuditId();
        Boolean approved = auditApproveDto.getApproved();
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User","id",userId));
        Audit audit = auditRepository.findById(auditId).orElseThrow(() -> new ResourceNotFoundException("Audit", "id", auditId));

        checkUserRoleToApprove(user,audit);

        AuditApprove newAuditApprove = new AuditApprove();
        newAuditApprove.setAudit(audit);
        newAuditApprove.setUser(user);
        newAuditApprove.setApproved(approved);
        newAuditApprove.setApprovalTime(LocalDateTime.now());

        AuditApprove savedAuditAppro =auditApproveRepository.save(newAuditApprove);

        List<AuditApprove> auditApproveList = auditApproveRepository.findByAuditId(audit.getId());
        boolean allApproved = auditApproveList.stream().allMatch(appro -> appro.isApproved());

        if(audit.getNumOfApprovalRequests() <= auditApproveList.size() && allApproved){
            audit.setStatus(1);
        }else if(audit.getNumOfApprovalRequests() <= auditApproveList.size()){
            audit.setStatus(2);
        }

        audit.setUpdateTime(LocalDateTime.now());
        Audit savedAudit = auditRepository.save(audit);

        return modelMapper.map(savedAudit,AuditDto.class);
    }

    private boolean checkUserRoleToApprove(User user, Audit audit){
        if(audit.getStatus() != 0){
            throw new UserPermissionNotAllowedException();
        }

        boolean isAdmin = user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN") );
        boolean isUser = user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_USER") );
        boolean repeatedApprove = audit.getApprovals().stream().anyMatch(approval -> approval.getUser().equals(user));
        if(!(isAdmin || isUser) || repeatedApprove){
            throw new UserPermissionNotAllowedException();
        }

        return true;
    }



}
