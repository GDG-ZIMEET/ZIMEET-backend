package com.gdg.z_meet.domain.user.service;

import com.gdg.z_meet.domain.user.entity.UserProfile;
import com.gdg.z_meet.domain.user.entity.enums.Level;
import com.gdg.z_meet.domain.user.repository.RefreshTokenRepository;
import com.gdg.z_meet.domain.user.repository.UserProfileRepository;
import com.gdg.z_meet.global.exception.BusinessException;
import com.gdg.z_meet.global.jwt.JwtUtil;
import com.gdg.z_meet.domain.user.dto.Token;
import com.gdg.z_meet.domain.user.dto.UserReq;
import com.gdg.z_meet.domain.user.dto.UserRes;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.domain.user.repository.UserRepository;
import com.gdg.z_meet.global.response.Code;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder;

    @Transactional
    public UserRes.SignUpRes signup(UserReq.SignUpReq signUpReq) {
        String password = signUpReq.getPassword();
        if (password == null || !password.matches("\\d{4,6}")
                || password.length() < 4 || password.length() > 6) {
            throw new BusinessException(Code.INVALID_PASSWORD);
        }
        if (isStudentNumberDuplicate(signUpReq.getStudentNumber())) {
            throw new BusinessException(Code.DUPLICATE_STUDENT_NUMBER);
        }
        if (isPhoneNumberDuplicate(signUpReq.getPhoneNumber())) {
            throw new BusinessException(Code.DUPLICATE_PHONE_NUMBER);
        }
        if (isNicknameDuplicate(signUpReq.getNickname())) {
            throw new BusinessException(Code.DUPLICATE_NICKNAME);
        }

        String encodedPassword = encoder.encode(signUpReq.getPassword());

        User user = User.builder()
                .studentNumber(signUpReq.getStudentNumber())
                .password(encodedPassword)
                .name(signUpReq.getName())
                .phoneNumber(signUpReq.getPhoneNumber())
                .build();
        User newUser = userRepository.save(user);

        UserProfile userProfile = UserProfile.builder()
                .nickname(signUpReq.getNickname())
                .emoji(signUpReq.getEmoji())
                .music(signUpReq.getMusic())
                .mbti(signUpReq.getMbti())
                .style(signUpReq.getStyle())
                .idealType(signUpReq.getIdealType())
                .idealAge(signUpReq.getIdealAge())
                .gender(signUpReq.getGender())
                .grade(signUpReq.getGrade())
                .major(signUpReq.getMajor())
                .age(signUpReq.getAge())
                .level(Level.LIGHT)
                .user(user)
                .build();
        userProfileRepository.save(userProfile);

        return UserRes.SignUpRes.builder().message("회원가입 성공!").build();
    }

    public Token login(UserReq.LoginReq loginReq, HttpServletResponse response) {
        User user = userRepository.findByStudentNumber(loginReq.getStudentNumber())
                .orElseThrow(() -> new BusinessException(Code.PROFILE_NOT_FOUND));

        if (!encoder.matches(loginReq.getPassword(), user.getPassword())) {
            throw new BusinessException(Code.INVALID_PASSWORD);
        }

        Token token = jwtUtil.createToken(loginReq.getStudentNumber(), user.getId());
        jwtUtil.createCookie(response, token.getRefreshToken());

        refreshTokenRepository.save(token.getRefreshToken(), user.getId(), jwtUtil.getRefreshTokenValidTime() / 1000);
        return token;
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtUtil.getRefreshTokenFromCookie(request);
        if (refreshToken != null) {
            refreshTokenRepository.delete(refreshToken);
        }
        clearRefreshTokenCookie(response);
    }

    @Transactional(readOnly = true)
    public UserRes.ProfileRes getProfile(Long userId) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(Code.PROFILE_NOT_FOUND));

        User user = userProfile.getUser();

        return UserRes.ProfileRes.builder()
                .id(user.getId())
                .name(user.getName())
                .studentNumber(user.getStudentNumber())
                .nickname(userProfile.getNickname())
                .phoneNumber(user.getPhoneNumber())
                .gender(userProfile.getGender())
                .emoji(userProfile.getEmoji())
                .mbti(userProfile.getMbti())
                .style(userProfile.getStyle())
                .idealType(userProfile.getIdealType())
                .idealAge(userProfile.getIdealAge())
                .grade(userProfile.getGrade())
                .major(userProfile.getMajor())
                .age(userProfile.getAge())
                .music(userProfile.getMusic())
                .level(userProfile.getLevel())
                .build();
    }

    @Transactional(readOnly = true)
    public UserRes.UserProfileRes getUserProfile(String nickname) {
        UserProfile userProfile = userProfileRepository.findByNickname(nickname)
                .orElseThrow(() -> new BusinessException(Code.PROFILE_NOT_FOUND));

        User user = userProfile.getUser();

        return UserRes.UserProfileRes.builder()
                .nickname(userProfile.getNickname())
                .studentNumber(user.getStudentNumber().substring(2,4))
                .gender(userProfile.getGender())
                .emoji(userProfile.getEmoji())
                .mbti(userProfile.getMbti())
                .style(userProfile.getStyle())
                .idealType(userProfile.getIdealType())
                .idealAge(userProfile.getIdealAge())
                .major(userProfile.getMajor().getShortName())
                .age(userProfile.getAge())
                .music(userProfile.getMusic())
                .build();
    }

    @CacheEvict(value = "userDetails", key = "#studentNumber")
    @Transactional
    public UserRes.NicknameUpdateRes updateNickname(Long userId, UserReq.NicknameUpdateReq request) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(Code.PROFILE_NOT_FOUND));

        userProfile.setNickname(request.getNickname());
        userProfileRepository.save(userProfile);

        return UserRes.NicknameUpdateRes.builder()
                .nickname(userProfile.getNickname())
                .build();
    }

    @CacheEvict(value = "userDetails", key = "#studentNumber")
    @Transactional
    public UserRes.EmojiUpdateRes updateEmoji(Long userId, UserReq.EmojiUpdateReq request) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(Code.PROFILE_NOT_FOUND));

        userProfile.setEmoji(request.getEmoji());
        userProfileRepository.save(userProfile);

        return UserRes.EmojiUpdateRes.builder()
                .emoji(userProfile.getEmoji())
                .build();
    }

    @Transactional
    public void withdraw(Long userId, HttpServletRequest request, HttpServletResponse response) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(Code.PROFILE_NOT_FOUND));

        userProfileRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);

        String refreshToken = jwtUtil.getRefreshTokenFromCookie(request);
        refreshTokenRepository.delete(refreshToken);

        clearRefreshTokenCookie(response);
    }

    public boolean isStudentNumberDuplicate(String studentNumber) {
        return userRepository.existsByStudentNumber(studentNumber);
    }

    public boolean isPhoneNumberDuplicate(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    public boolean isNicknameDuplicate(String nickname) {
        return userProfileRepository.existsByNickname(nickname);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        log.info("Refresh token cookie cleared");
    }
}