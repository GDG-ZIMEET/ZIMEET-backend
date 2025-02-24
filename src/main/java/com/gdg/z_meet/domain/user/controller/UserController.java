package com.gdg.z_meet.domain.user.controller;

import com.gdg.z_meet.global.exception.GlobalException;
import com.gdg.z_meet.global.jwt.JwtUtil;
import com.gdg.z_meet.global.response.Response;
import com.gdg.z_meet.domain.user.dto.Token;
import com.gdg.z_meet.domain.user.dto.UserReq;
import com.gdg.z_meet.domain.user.dto.UserRes;
import com.gdg.z_meet.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "User", description = "User API")
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

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
    public Response<UserRes.LoginRes> login(@RequestBody UserReq.LoginReq loginReq, HttpServletResponse response) {
        try {
            Token token = userService.login(loginReq, response);
            UserRes.LoginRes loginRes = UserRes.LoginRes.builder()
                    .accessToken(token.getAccessToken())
                    .key(token.getKey())
                    .userId(token.getUserId())
                    .build();
            return Response.ok(loginRes);
        } catch (GlobalException exception) {
            return Response.fail(exception.getCode());
        }
    }

    @DeleteMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그아웃")
    public Response<Void> logout(HttpServletResponse response, @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String accessToken = authorizationHeader.substring(7);

            userService.logout(response, accessToken);
            return Response.ok(null);
        } catch (GlobalException exception) {
            return Response.fail(exception.getCode());
        }
    }

    @GetMapping("/myprofile")
    @Operation(summary = "내 세부 프로필 조회", description = "내 세부 프로필 조회")
    public Response<UserRes.ProfileRes> getProfile(HttpServletRequest request) {
        try {
            Long userId = jwtUtil.extractUserIdFromRequest(request);
            return Response.ok(userService.getProfile(userId));
        } catch (GlobalException exception) {
            return Response.fail(exception.getCode());
        }
    }

    @GetMapping("profile/{nickname}")
    @Operation(summary = "유저 프로필 조회", description = "유저 프로필 조회")
    public Response<UserRes.UserProfileRes> getProfile(@PathVariable String nickname) {
        try {
            return Response.ok(userService.getUserProfile(nickname));
        } catch (GlobalException exception) {
            return Response.fail(exception.getCode());
        }
    }

    @PatchMapping("/myprofile/nickname")
    @Operation(summary = "내 닉네임 수정", description = "내 닉네임 수정")
    public Response<UserRes.NicknameUpdateRes> updateNickname(
            HttpServletRequest request,
            @Valid @RequestBody UserReq.NicknameUpdateReq nicknameUpdateReq) {
        try {
            Long userId = jwtUtil.extractUserIdFromRequest(request);
            return Response.ok(userService.updateNickname(userId, nicknameUpdateReq));
        } catch (GlobalException exception) {
            return Response.fail(exception.getCode());
        }
    }

    @PatchMapping("/myprofile/emoji")
    @Operation(summary = "내 이모지 수정", description = "내 이모지 수정")
    public Response<UserRes.EmojiUpdateRes> updateEmoji(
            HttpServletRequest request,
            @Valid @RequestBody UserReq.EmojiUpdateReq emojiUpdateReq) {
        try {
            Long userId = jwtUtil.extractUserIdFromRequest(request);
            return Response.ok(userService.updateEmoji(userId, emojiUpdateReq));
        } catch (GlobalException exception) {
            return Response.fail(exception.getCode());
        }
    }

    @DeleteMapping("/withdraw")
    @Operation(summary = "회원탈퇴", description = "회원탈퇴")
    public Response<Void> withdraw(HttpServletRequest request, HttpServletResponse response) {
        try {
            Long userId = jwtUtil.extractUserIdFromRequest(request);
            userService.withdraw(userId, response);
            return Response.ok(null);
        } catch (GlobalException exception) {
            return Response.fail(exception.getCode());
        }
    }
}
