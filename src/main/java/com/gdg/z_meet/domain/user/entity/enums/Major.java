package com.gdg.z_meet.domain.user.entity.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Major {
    // 인문계열
    KOREAN("국어국문학과"), PHILOSOPHY("철학과"), KOREANHISTORY("국사학과"), CHILDREN("아동학과"),
    // 어문계열
    ENGLISH("영어영문학과"), CHINESE("중어중문학과"), JAPANESE("일어일문학과"), FRENCH("불어불문학과"),
    // 음악과
    MUSIC("음악과"),
    // 신학과
    THEOLOGY("신학과"),

    SOCIALWELFARE("사회복지학과"), PSYCHOLOGY("심리학과"), SOCIOLOGY("사회학과"), SPECIAL_EDUCATION("특수교육과"),
    // 경영계열
    BUSINESS("경영학과"), ACCOUNTING("회계학과"),
    // 국제, 법정경계열
    INTERNATIONAL_STUDIES("국제학과"), LAW("법학과"), ECONOMICS("경제학과"), PUBLIC_ADMINISTRATION("행정학과"),
    // 글로벌경영대학
    GBS("글로벌경영학부"),

    // 자연과학계열
    CHEMISTRY("화학과"), MATH("수학과"), PHYSICS("물리학과"),
    // 생명과학계열
    DESIGN("공간디자인소비자학과"), CLOTHING("의류학과"), FOOD_NUTRITION("식품영양학과"),
    // 의생명과학과
    MEDICAL_BIOLOGY("의생명과학과"),
    // 약학대학
    PHARMACY("약학과"),
    // 간호대, 의대
    NURSING("간호학과"), MEDICINE("의학과"),

    // ICT 공학계열
    CSIE("컴퓨터정보공학과"), MTC("미디어기술콘텐츠학과"), ICE("정보통신전자공학과"),
    // 바이오융합공학계열
    BIOTECH("바이오메디컬화학공학과"), ENVI("에너지환경공학과"), BMCE("생명공학과"),
    // 인공지능, 데이터사이언스, 바이오메디컬소프트웨어
    AI("인공지능학과"), DA("데이터사이언스학과"), BMSW("바이오메디컬소프트웨어학과");

    private final String displayName;

    Major(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }
}
