package com.great.jinuk.domain.svc.comment;

import com.great.jinuk.domain.dao.comment.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentSVC_old {

  /**
   * 댓글 조회
   * @param commentNum 댓글 번호
   * @return 댓글
   */
  Optional<Comment> find(Long commentNum);

  /**
   * 게시글에 달린 댓글 목록 조회
   * @return 댓글 목록
   */
  List<Comment> findAll(Long articleNum);

  /**
   * 댓글 작성
   * @param comment 댓글 정보
   * @return 댓글 정보
   */
  Comment save(Comment comment);


//  /**
//   * 대댓글 작성 (필요할까?)
//   * @param replyComment 댓글 정보
//   * @return 댓글 정보
//   */
//  Comment saveReply(Long pCommentNum, Comment replyComment);

  /**
   * 댓글 수정
   * @param commentNum 댓글 번호
   * @param comment 댓글 정보
   * @return
   */
  Comment update(Long commentNum, Comment comment);


  /**
   * 댓글 삭제
   * @param commentNum 댓글 번호
   * @return 댓글 내용
   */
  void delete(Long commentNum);


//  /**
//   * 게시물 댓글 건수 조회
//   * @param articleNum 게시글 번호
//   * @return 댓글 건수
//   */
//  int totalCountOfArticle(Long articleNum);
}
