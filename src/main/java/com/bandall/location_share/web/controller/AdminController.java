package com.bandall.location_share.web.controller;

import com.bandall.location_share.domain.admin.AdminService;
import com.bandall.location_share.domain.admin.dto.MemberInfo;
import com.bandall.location_share.domain.admin.dto.RoleInfo;
import com.bandall.location_share.domain.member.UserPrinciple;
import com.bandall.location_share.web.controller.dto.PageDto;
import com.bandall.location_share.web.controller.dto.RoleModifyDto;
import com.bandall.location_share.web.controller.dto.SessionInfoDto;
import com.bandall.location_share.web.controller.json.ApiResponseJson;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/api/admin/members")
    public ApiResponseJson getMemberInfos(@AuthenticationPrincipal UserPrinciple userPrinciple,
                                          @RequestParam(required = false, defaultValue = "0") int page,
                                          @RequestParam(required = false, defaultValue = "10") int size) {
        PageDto<MemberInfo> memberInfos = adminService.getMemberInfos(userPrinciple.getEmail(), page, size);

        return new ApiResponseJson(HttpStatus.OK, memberInfos);
    }

    @GetMapping("/api/admin/member")
    public ApiResponseJson getMemberInfo(@AuthenticationPrincipal UserPrinciple userPrinciple, @RequestParam(required = false) String email) {
        log.info("Fetching member info of user: {}", userPrinciple.getEmail());
        MemberInfo memberInfo = adminService.getMemberInfo(userPrinciple.getEmail(), email);

        return new ApiResponseJson(HttpStatus.OK, memberInfo);
    }

    @GetMapping("/api/admin/roles")
    public ApiResponseJson getRoles(@AuthenticationPrincipal UserPrinciple userPrinciple) {
        RoleInfo roles = adminService.getRoles(userPrinciple.getEmail());

        return new ApiResponseJson(HttpStatus.OK, roles);
    }

    @PostMapping("/api/admin/member/role")
    public ApiResponseJson modifyMemberRole(@Valid @RequestBody RoleModifyDto roleModifyDto, BindingResult bindingResult,
                                            @AuthenticationPrincipal UserPrinciple userPrinciple) {
        checkDto(bindingResult);

        if (roleModifyDto.getAction() == RoleModifyDto.RoleAction.ADD_ROLE) {
            log.info("Adding role for user: {}, role: {}", userPrinciple.getEmail(), roleModifyDto.getEmail());
            adminService.addRole(userPrinciple.getEmail(), roleModifyDto.getEmail(), roleModifyDto.getRole());
        }

        if (roleModifyDto.getAction() == RoleModifyDto.RoleAction.REMOVE_ROLE) {
            log.info("Removing role for user: {}, role: {}", userPrinciple.getEmail(), roleModifyDto.getEmail());
            adminService.removeRole(userPrinciple.getEmail(), roleModifyDto.getEmail(), roleModifyDto.getRole());
        }

        return new ApiResponseJson(HttpStatus.OK, "OK");
    }

    @GetMapping("/api/admin/session")
    public ApiResponseJson getSessionInfo(@RequestParam(required = false, defaultValue = "1") int page,
                                          @RequestParam(required = false, defaultValue = "10") int size,
                                          @AuthenticationPrincipal UserPrinciple userPrinciple) {
        PageDto<SessionInfoDto> sessionInfos = adminService.getSessionInfos(userPrinciple.getEmail(), page, size);

        return new ApiResponseJson(HttpStatus.OK, sessionInfos);
    }

    @GetMapping("/api/admin/session/ban")
    public ApiResponseJson setTokenBlacklist(@RequestParam String tokenId,
                                             @AuthenticationPrincipal UserPrinciple userPrinciple) {
        adminService.setTokenBlacklist(userPrinciple.getEmail(), tokenId);

        return new ApiResponseJson(HttpStatus.OK, "OK");
    }

    private void checkDto(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.info("Invalid arguments received.");
            throw new IllegalArgumentException("잘못된 인자값입니다.");
        }
    }

}