package net.javaspring.ems.service.impl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import net.javaspring.ems.dto.AuditApproveDto;
import net.javaspring.ems.dto.AuditDto;
import net.javaspring.ems.entity.Audit;
import net.javaspring.ems.entity.AuditApprove;
import net.javaspring.ems.entity.Role;
import net.javaspring.ems.entity.User;
import net.javaspring.ems.exception.ResourceNotFoundException;
import net.javaspring.ems.exception.UserPermissionNotAllowedException;
import net.javaspring.ems.repository.AuditApproveRepository;
import net.javaspring.ems.repository.AuditRepository;
import net.javaspring.ems.repository.UserRepository;
import net.javaspring.ems.service.AuditService;
import net.javaspring.ems.utils.AuditType;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

        User user = userRepository.findById(auditDto.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User","id",auditDto.getUserId()));
        Audit audit = new Audit();
        audit.setAmountMoney(auditDto.getAmountMoney());
        audit.setTitle(auditDto.getTitle());
        audit.setContent(auditDto.getContent());
        audit.setRequireAllApprovalPassing(auditDto.isRequireAllApprovalPassing());
        audit.setAllowedToLeapfrog(auditDto.isAllowedToLeapfrog());
        audit.setRequirePeerReview(auditDto.isRequirePeerReview());
        audit.setUser(user);

        //确定审批表的类型 去确定需要审批的人
        AuditType auditType = AuditType.fromValue(auditDto.getAuditType());
        audit.setAuditType(auditType);
        audit.setStatus(1);
        List<AuditApprove> auditApproveList= createAuditApprovalList(audit, user);
        audit.setApprovals(auditApproveList);

        audit.setCreateTime(LocalDateTime.now());
        Audit savedAudit = auditRepository.save(audit);


        auditDto.setApprovals(convertAuditApproveListToDto(savedAudit.getApprovals()));
        auditDto.setStatus(audit.getStatus());
        auditDto.setCreateTime(audit.getCreateTime());
        return auditDto;
    }

    /**
     * 查询单一audit中所有approvals信息
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
     * 查询单一audit表的信息
     *
     * @param auditId 此表的ID
     * @return 查询结果
     */

    @Override
    public AuditDto getAuditById(Long auditId) {

        Audit audit = auditRepository.findById(auditId).orElseThrow(() -> new ResourceNotFoundException("Audit","id",auditId));
        return modelMapper.map(audit,AuditDto.class);
    }

    /**
     * 查询所有审查的记录
     *
     * @return 查询结果
     */
    @Override
    public List<AuditDto> getAllAudit() {

        List<Audit> auditList = auditRepository.findAll();
        return auditList.stream().map(audit -> convertAuditToAuditDto(audit)).collect(Collectors.toList());
    }

    /**
     * 用户批准 批准逻辑
     *
     * @param auditApproveDto 用户填用的dto
     * @return 当前审查状态
     */

    @Override
    public AuditDto userApproveAudit(AuditApproveDto auditApproveDto) {

        String approved = auditApproveDto.getApproved();
        User user = userRepository.findById(auditApproveDto.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User","id",auditApproveDto.getUserId()));
        Audit audit = auditRepository.findById(auditApproveDto.getAuditId()).orElseThrow(() -> new ResourceNotFoundException("Audit", "id", auditApproveDto.getAuditId()));

        // 先判断能否自己就能通过此审批 可放在create的后面
        if(audit.getUser() == user){
            if((audit.getAuditType().getLevel() <= convertAllRolesToMaxNum(user.getRoles()) && !audit.isRequirePeerReview()) ||
                    (audit.getAuditType().getLevel() < convertAllRolesToMaxNum(user.getRoles()) && audit.isRequirePeerReview())){
                audit.setStatus(0);
                audit.setUpdateTime(LocalDateTime.now());
                Audit savedAudit = auditRepository.save(audit);
                return modelMapper.map(savedAudit,AuditDto.class);
            }else{
                throw new UserPermissionNotAllowedException(user.getId());
            }
        }

        // 判断其他人是否有审批权限
        checkUserRoleForApprove(user,audit);

        AuditApprove newAuditApprove = createAuditApprove(audit,user,auditApproveDto.getAuditLevelOrder(),auditApproveDto.isLastLevel(),auditApproveDto.getApprovedStatus());
        newAuditApprove.setApproved(approved);
        newAuditApprove.setApprovedStatus("complete");
        newAuditApprove.setApprovalTime(LocalDateTime.now());
        auditApproveRepository.save(newAuditApprove);

        //当允许跨级审批并且审批人等级高于目标要求时 审批可直接通过
        if(audit.isAllowedToLeapfrog() && audit.getAuditType().getLevel() < convertAllRolesToMaxNum(user.getRoles())){
            if(newAuditApprove.getApproved().equals("approved")) {
                audit.setStatus(2);
                audit.setApprovals(updateApprovalListStatus(audit.getApprovals(), "cancel"));
            }
        }else{
            //判断目前审批等级是否可到下一级 或者是否审批单已经完成审批
            checkAuditStatus(audit,newAuditApprove);
        }

        audit.setUpdateTime(LocalDateTime.now());
        Audit savedAudit = auditRepository.save(audit);
        AuditDto auditDto = modelMapper.map(savedAudit,AuditDto.class);
        auditDto.setApprovals(convertAuditApproveListToDto(savedAudit.getApprovals()));
        return auditDto;
    }

    private void checkAuditStatus(Audit audit, AuditApprove newAuditApprove){
        List<AuditApprove> auditApprovals = audit.getApprovals();

        // 检查是否用户批准的当前层的所有人 都批了refused
        boolean checkCurLevelAllFalse = auditApprovals.stream()
                .noneMatch(appro -> appro.getAuditLevelOrder() == newAuditApprove.getAuditLevelOrder() && appro.getApproved().equals("approved"));

        //审批需要所有人通过 当用户拒绝此审批时 和 当前层所有人都拒绝
        if((audit.isRequireAllApprovalPassing() && newAuditApprove.getApproved().equals("refused")) || (checkCurLevelAllFalse)){
            audit.setStatus(3);
            //把waiting和process改成cancel 因为已经完成了
            auditApprovals = updateApprovalListStatus(auditApprovals, "cancel");
        } // 审批所有全部通过时 或者 当不需要所有人通过时 最后一层有一人通过时  结束此审批并通过
        else if(auditApprovals.stream().allMatch(appro -> appro.getApproved().equals("approved"))){
            audit.setStatus(2);
        }else if((!audit.isRequireAllApprovalPassing() && newAuditApprove.isLastLevel() && newAuditApprove.getApproved().equals("approved"))){
            auditApprovals = updateApprovalListStatus(auditApprovals, "cancel");
        }

        audit.setApprovals(auditApprovals);
        auditRepository.save(audit);
    }

    public List<AuditApprove> updateApprovalListStatus(List<AuditApprove> approvals, String status) {
        return approvals.stream()
                .map(appro -> {
                    if (appro.getApprovedStatus().equals("waiting") || appro.getApprovedStatus().equals("process")) {
                        appro.setApprovedStatus(status);
                    }
                    return appro;
                })
                .collect(Collectors.toList());
    }

    private boolean checkUserRoleForApprove(User user, Audit audit){
        //必须在审批阶段才能审批
        if(audit.getStatus() != 1){
            throw new UserPermissionNotAllowedException(user.getId());
        }

        //审批人
        int userLevel = convertAllRolesToMaxNum(user.getRoles());
        int applicantLevel =  convertAllRolesToMaxNum(audit.getUser().getRoles());
        // 审批人最低从哪一级开始审批
        int lowestLevel = audit.isRequirePeerReview() ? applicantLevel : applicantLevel + 1;

        // 审批人的等级是否能批准此单子 审批人是否已经审批过此单子
        boolean userLevelRequirement = audit.isRequirePeerReview() ? userLevel >= applicantLevel : userLevel > applicantLevel;
        boolean repeatedApprove = audit.getApprovals().stream().anyMatch(approval -> approval.getUser().equals(user));

        if(userLevelRequirement || repeatedApprove){
            throw new UserPermissionNotAllowedException(user.getId());
        }

        // 不能越级审批时，越级会报错
        if(!audit.isAllowedToLeapfrog() && userLevel != lowestLevel){
            List<AuditApprove> lastLevelApprovals = auditApproveRepository.findAllCurrentLevelApprovals(audit,userLevel - 1);

            //如果审批需要所有人通过，找到上一级中一个waiting说明上一级的审批还没有完毕，说明跳级了
            if(audit.isRequireAllApprovalPassing() && lastLevelApprovals.stream().anyMatch(approval -> approval.getApprovedStatus().equals("waiting"))){
                throw new UserPermissionNotAllowedException(user.getId());
            }//如果审批只需要任意人通过，如果找不到上一级任何complete，说明跳级了
            else if(!audit.isRequireAllApprovalPassing() && lastLevelApprovals.stream().noneMatch(approval -> approval.getApprovedStatus().equals("complete"))){
                throw new UserPermissionNotAllowedException(user.getId());
            }
        }


        //boolean isAdmin = user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN") );
        //boolean isUser = user.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_USER") );


        return true;
    }

    /**
     *
     *  生成所有需要审批的对象
     *
     * @param audit
     * @param user
     * @return
     */

    private List<AuditApprove> createAuditApprovalList(Audit audit, User user){
        List<AuditApprove> auditApproveList = new ArrayList<>();

        boolean isLastLevel = false;
        // 0 : 同级审批  1 : 跳过同级
        int peerReview = audit.isRequirePeerReview() ? 0 : 1;
        // 此员工开始审批的等级
        int startLevel = convertAllRolesToMaxNum(user.getRoles()) + peerReview;

        for (int level = startLevel ; level <= audit.getAuditType().getLevel(); level++){
            if(level == audit.getAuditType().getLevel()) isLastLevel = true;

            String status = "waiting";
            if(audit.isAllowedToLeapfrog() || level == startLevel) status = "process";

            List<User> curLevelUser = userRepository.findByRoleName(convertNumToRoleName(level));
            for(User curUser : curLevelUser){
                AuditApprove auditApprove = createAuditApprove(audit,curUser,level,isLastLevel,status);
                auditApproveList.add(auditApprove);
            }
        }
        return auditApproveList;
    }

    private AuditApprove createAuditApprove(Audit audit, User user, int level ,boolean isLastLevel, String status){
        AuditApprove auditApprove = new AuditApprove();
        auditApprove.setAudit(audit);
        auditApprove.setUser(user);
        auditApprove.setApprovedStatus(status);
        auditApprove.setAuditLevelOrder(level);
        auditApprove.setLastLevel(isLastLevel);
        auditApprove.setApproved("");

        return auditApprove;

    }

    private List<AuditApproveDto> convertAuditApproveListToDto(List<AuditApprove> list){
        List<AuditApproveDto> dtoList = new ArrayList<>();

        for (AuditApprove auditApprove : list){
            AuditApproveDto auditApproveDto = new AuditApproveDto();
            auditApproveDto.setAuditId(auditApprove.getAudit().getId());
            auditApproveDto.setUserId(auditApprove.getUser().getId());
            auditApproveDto.setApprovedStatus(auditApprove.getApprovedStatus());
            auditApproveDto.setApproved("");
            auditApproveDto.setAuditLevelOrder(auditApprove.getAuditLevelOrder());
            auditApproveDto.setLastLevel(auditApprove.isLastLevel());

            dtoList.add(auditApproveDto);
        }

        return dtoList;
    }


    private int convertAllRolesToMaxNum(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new ResourceNotFoundException("User", "roleNotFound", null);
        }

        int max = -1;

        for (Role role : roles){
            String name = role.getName().toLowerCase();

            switch (name) {
                case "regular":
                    max = Math.max(1,max);
                    break;
                case "group leader":
                    max = Math.max(2,max);
                    break;
                case "manager":
                    max = Math.max(3,max);
                    break;
                case "director":
                    max = Math.max(4,max);
                    break;
                case "president":
                    max = Math.max(5,max);
                    break;
                case "role_admin": break;// 排除admin
                case "role_user": break;// 排除user

            }
        }
        return max;
    }

    private String convertNumToRoleName(int num){
        return switch (num) {
            case 1 -> "REGULAR";
            case 2 -> "GROUP_LEADER";
            case 3 -> "MANAGER";
            case 4 -> "DIRECTOR";
            case 5 -> "PRESIDENT";
            default -> throw new ResourceNotFoundException("Role", "RoleNum", (long) num);
        };
    }

    private AuditDto convertAuditToAuditDto(Audit audit){
        AuditDto auditDto = new AuditDto();

        // 手动进行属性赋值
        auditDto.setAuditType(audit.getAuditType().getLevel());
        auditDto.setApprovals(convertAuditApproveListToDto(audit.getApprovals()));
        auditDto.setAmountMoney(audit.getAmountMoney());
        auditDto.setTitle(audit.getTitle());
        auditDto.setContent(audit.getContent());
        auditDto.setAllowedToLeapfrog(audit.isAllowedToLeapfrog());
        auditDto.setRequirePeerReview(audit.isRequirePeerReview());
        auditDto.setRequireAllApprovalPassing(audit.isRequireAllApprovalPassing());
        auditDto.setStatus(audit.getStatus());
        auditDto.setUserId(audit.getId());
        auditDto.setCreateTime(audit.getCreateTime());
        auditDto.setUpdateTime(audit.getUpdateTime());

        return auditDto;
    }



}
