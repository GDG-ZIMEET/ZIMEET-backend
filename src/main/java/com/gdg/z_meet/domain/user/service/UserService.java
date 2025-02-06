package com.gdg.z_meet.domain.user.service;

import com.gdg.z_meet.domain.user.entity.UserProfile;
import com.gdg.z_meet.domain.user.entity.enums.Level;
import com.gdg.z_meet.domain.user.repository.UserProfileRepository;
import com.gdg.z_meet.global.jwt.JwtUtil;
import com.gdg.z_meet.domain.user.dto.Token;
import com.gdg.z_meet.domain.user.dto.UserReq;
import com.gdg.z_meet.domain.user.dto.UserRes;
import com.gdg.z_meet.domain.user.entity.RefreshToken;
import com.gdg.z_meet.domain.user.entity.User;
import com.gdg.z_meet.domain.user.repository.RefreshTokenRepository;
import com.gdg.z_meet.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
        userRepository.findByStudentNumber(signUpReq.getStudentNumber())
                .ifPresent(user -> {
                    throw new IllegalArgumentException("이미 가입된 학번입니다.");
                });

        userProfileRepository.findByNickname(signUpReq.getNickname())
                .ifPresent(user -> {
                    throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
                });

        String encodedPassword = encoder.encode(signUpReq.getPassword());

        User user = User.builder()
                .studentNumber(signUpReq.getStudentNumber())
                .password(encodedPassword)
                .name(signUpReq.getName())
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

    @Transactional
    public Token login(UserReq.LoginReq loginReq) {
        User user = userRepository.findByStudentNumber(loginReq.getStudentNumber())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 학번입니다."));

        if (!encoder.matches(loginReq.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        Token token = jwtUtil.createToken(loginReq.getStudentNumber(), user.getId());

        saveRefreshToken(token);  // 트랜잭션 적용된 메서드 호출

        return token;
    }

    @Transactional
    public void saveRefreshToken(Token token) {
        Optional<RefreshToken> tokenOptional = refreshTokenRepository.findByKeyId(token.getKey());

        if (tokenOptional.isEmpty()) {
            refreshTokenRepository.save(
                    RefreshToken.builder()
                            .keyId(token.getKey())
                            .refreshToken(token.getRefreshToken())
                            .build());
        } else {
            tokenOptional.get().update(token.getRefreshToken());
        }
    }


    @Transactional
    public UserRes.ProfileRes getProfile(Long userId) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필이 존재하지 않습니다."));

        User user = userProfile.getUser();

        return UserRes.ProfileRes.builder()
                .name(user.getName())
                .studentNumber(user.getStudentNumber())
                .nickname(userProfile.getNickname())
                .emoji(userProfile.getEmoji())
//                .music(userProfile.getMusic())
                .mbti(userProfile.getMbti())
                .style(userProfile.getStyle())
                .idealType(userProfile.getIdealType())
                .idealAge(userProfile.getIdealAge())
//                .gender(userProfile.getGender())
                .grade(userProfile.getGrade())
                .major(userProfile.getMajor())
                .age(userProfile.getAge())
//                .level(userProfile.getLevel())
                .build();
    }

    @Transactional
    public UserRes.NicknameUpdateRes updateNickname(Long userId, UserReq.NicknameUpdateReq request) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필이 존재하지 않습니다."));

        userProfile.setNickname(request.getNickname());
        userProfileRepository.save(userProfile);

        return UserRes.NicknameUpdateRes.builder()
                .nickname(userProfile.getNickname())
                .build();
    }

    @Transactional
    public UserRes.EmojiUpdateRes updateEmoji(Long userId, UserReq.EmojiUpdateReq request) {
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("프로필이 존재하지 않습니다."));

        userProfile.setEmoji(request.getEmoji());
        userProfileRepository.save(userProfile);

        return UserRes.EmojiUpdateRes.builder()
                .emoji(userProfile.getEmoji())
                .build();
    }
}
