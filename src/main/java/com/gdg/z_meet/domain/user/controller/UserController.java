package com.gdg.z_meet.domain.user.controller;

import com.gdg.z_meet.global.exception.GlobalException;
import com.gdg.z_meet.global.response.Response;
import com.gdg.z_meet.domain.user.dto.Token;
import com.gdg.z_meet.domain.user.dto.UserReq;
import com.gdg.z_meet.domain.user.dto.UserRes;
import com.gdg.z_meet.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "User", description = "User API")
@RequestMapping("/jwt")
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "회원가입")
    public Response<UserRes.SignUpRes> signup(@RequestBody UserReq.SignUpReq signUpReq) {
        try {
            return Response.ok(userService.signup(signUpReq));
        } catch (GlobalException exception) {
            return Response.fail(exception.getCode());
        }
    }
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "로그인")
    public Response<Token> login(@RequestBody UserReq.LoginReq loginReq) {
        try {
            return Response.ok(userService.login(loginReq));
        } catch (GlobalException exception) {
            return Response.fail(exception.getCode());
        }
    }
}
