package net.javaspring.ems.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import net.javaspring.ems.dto.AuditApproveDto;
import net.javaspring.ems.dto.AuditDto;
import net.javaspring.ems.security.JwtAuthenticationFilter;
import net.javaspring.ems.security.JwtTokenProvider;
import net.javaspring.ems.service.AuditService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@AllArgsConstructor
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private AuditService auditService;

    private JwtTokenProvider jwtTokenProvider;



    /**
     * 用户生成审批单
     *
     * @param auditDto
     * @return
     */
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/create")
    public ResponseEntity<AuditDto> createAudit(@RequestBody AuditDto auditDto, HttpServletRequest request){
        String token = request.getHeader("Authorization");
        if(jwtTokenProvider.validateToken(token)){
            String userName = jwtTokenProvider.getUsername(token);
            auditDto.getUserId();
        }


        AuditDto audit = auditService.createAudit(auditDto);
        return new ResponseEntity<>(audit, HttpStatus.CREATED);
    }


    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping({"id"})
    public ResponseEntity<AuditDto> getAuditById(@PathVariable Long auditId){
        AuditDto audit = auditService.getAuditById(auditId);

        return new ResponseEntity<>(audit, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping
    public ResponseEntity<List<AuditDto>> getAllAudit(){
        List<AuditDto> auditList = auditService.getAllAudit();
        return new ResponseEntity<>(auditList, HttpStatus.OK);
    }

    /**
     * 审批人 审批对应的审批单
     *
     * @param auditApproveDto
     * @return
     */

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping
    public ResponseEntity<AuditDto> userApproveAudit(@RequestBody AuditApproveDto auditApproveDto){
        AuditDto audit = auditService.userApproveAudit(auditApproveDto);
        return new ResponseEntity<>(audit, HttpStatus.OK);
    }

}
