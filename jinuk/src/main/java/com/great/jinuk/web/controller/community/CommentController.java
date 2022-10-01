package com.great.jinuk.web.controller.community;

import com.great.jinuk.domain.common.AttachCode;
import com.great.jinuk.domain.dao.comment.Comment;
import com.great.jinuk.domain.entity.uploadFile.UploadFile;
import com.great.jinuk.domain.svc.comment.CommentSVC;
import com.great.jinuk.domain.svc.uploadFile.UploadFileSVC;
import com.great.jinuk.web.api.ApiResponse;
import com.great.jinuk.web.form.article.ArticleForm;
import com.great.jinuk.web.form.comment.CommentAddForm;
import com.great.jinuk.web.form.comment.CommentEditForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

  private final CommentSVC commentSVC;
  private final UploadFileSVC uploadFileSVC;

  //댓글 목록 조회
  @GetMapping("/list/{articleNum}")
  public ApiResponse<List<Comment>> commentList(@PathVariable("articleNum") Long articleNum) {

    List<Comment> commentList = commentSVC.findAll(articleNum);

    for (Comment comment :
        commentList) {
      List<UploadFile> uploadFile = uploadFileSVC.getFilesByCodeWithRid(AttachCode.P0101.name(), comment.getCommentNum());
      if (uploadFile.size()>0) {
        UploadFile attachFile = uploadFile.get(0);
        comment.setAttachFile(attachFile);
      }
    }

    //2)첨부파일 조회
    //2-1)상품설명파일 조회
    List<UploadFile> uploadFile = uploadFileSVC.getFilesByCodeWithRid(AttachCode.P0101.name(), productId);
    if (uploadFile.size()>0) {
      UploadFile attachFile = uploadFile.get(0);
      detailForm.setAttachFile(attachFile);
    }

    return ApiResponse.createApiResMsg("00","성공", commentList);
  }

  //댓글 등록
  @PostMapping("/write/{articleNum}")
  public ApiResponse<Comment> saveComment(@PathVariable("articleNum") Long articleNum,
                                          CommentAddForm commentAddForm) {

    Comment comment = new Comment();
    BeanUtils.copyProperties(commentAddForm,comment);
    comment.setArticleNum(articleNum); //꼭 필요할까?

    //댓글 등록
    Comment savedComment = new Comment();

    //주의 : view에서 multiple인 경우 파일 첨부가 없더라도 빈문자열("")이 반환되어
    // List<MultipartFile>에 빈 객체 1개가 포함됨
    if (commentAddForm.getFile().isEmpty()) {
      savedComment = commentSVC.save(comment);
    } else if (!commentAddForm.getFile().isEmpty()) {
      savedComment = commentSVC.save(comment,commentAddForm.getFile());
    }

    return ApiResponse.createApiResMsg("00","성공", savedComment);
  }

  //대댓글 등록
  @PostMapping("/reply")
  public ApiResponse<Comment> saveReplyComment(ArticleForm articleForm,
                                               CommentAddForm commentAddForm) {

    Comment comment = new Comment();
    BeanUtils.copyProperties(commentAddForm,comment);
    comment.setArticleNum(articleForm.getArticleNum()); //꼭 필요할까?

    //대댓글 등록
    Comment savedReplyComment = commentSVC.saveReply(comment.getPCommentNum(), comment);


    return ApiResponse.createApiResMsg("00","성공", savedReplyComment);
  }

  //댓글 수정 화면

  @PatchMapping("/edit/{commentNum}")
  //댓글 수정 처리
  public ApiResponse<Comment> editComment(@PathVariable Long commentNum,
                                          CommentEditForm commentEditForm) {

    Comment comment = new Comment();
    BeanUtils.copyProperties(commentEditForm,comment);

    //댓글 수정
    Comment updatedComment = new Comment();

    //주의 : view에서 multiple인 경우 파일 첨부가 없더라도 빈문자열("")이 반환되어
    // List<MultipartFile>에 빈 객체 1개가 포함됨
    if (commentEditForm.getFile().isEmpty()) {
      updatedComment = commentSVC.update(commentNum, comment);
    } else if (!commentEditForm.getFile().isEmpty()) {
      updatedComment = commentSVC.update(commentNum, comment,commentEditForm.getFile());
    }

    return ApiResponse.createApiResMsg("00","성공", updatedComment);
  }

  @DeleteMapping("/delete/{commentNum}")
  //댓글 삭제
  public ApiResponse<Comment> deleteComment(@PathVariable Long commentNum){

    commentSVC.delete(commentNum);

    return ApiResponse.createApiResMsg("00","성공", null);
  }

}
