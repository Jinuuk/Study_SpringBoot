package com.kh.myapp3.domain.svc;


import com.kh.myapp3.domain.dao.Member;

import java.util.Optional;

public interface MemberSVC {
  /**
   * 가입
   * @param member 가입정보
   * @return 회원아이디
   */
  Member insert(Member member);

  /**
   * 회원정보 조회 by 회원아이디
   * @param memberId
   * @return 회원정보
   */
  Member findById(Long memberId);

  /**
   * 회원정보 수정
   * @param memberId 아이디
   * @param member 수정할 정보
   * @return 수정건수
   */
  int update(Long memberId, Member member);

  /**
   * 탈퇴
   * @param memberId 아이디
   */
  int del(Long memberId, String pw);

  /**
   * 로그인
   * @param email 이메일
   * @param pw 비밀번호
   * @return 회원
   */
  Optional<Member> login(String email, String pw);




}
