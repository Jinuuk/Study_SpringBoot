package com.great.jinuk.domain.dao.comment;

import com.great.jinuk.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CommentDAOImpl implements CommentDAO {

  private final JdbcTemplate jt;

  /**
   * 댓글 조회
   *
   * @param commentNum 댓글 번호
   * @return 댓글
   */
  @Override
  public Optional<Comment> find(Long commentNum) {
    StringBuffer sql = new StringBuffer();

    sql.append("select article_num, comment_group, comment_num, p_comment_num, step, comment_order, ");
    sql.append("p_comment_nickname, m.mem_nickname, comment_contents, create_date, reply ");
    sql.append("from comments c, member m ");
    sql.append("where c.mem_number = m.mem_number and c.comment_num = ? ");

    try {
      Comment comment = jt.queryForObject(sql.toString(), new RowMapper<Comment>() {
        @Override
        public Comment mapRow(ResultSet rs, int rowNum) throws SQLException {
          Member member = (new BeanPropertyRowMapper<>(Member.class)).mapRow(rs, rowNum);
          Comment comment = (new BeanPropertyRowMapper<>(Comment.class)).mapRow(rs, rowNum);
          comment.setMember(member);
          return comment;
        }
      }, commentNum);
      return Optional.of(comment);
    } catch (EmptyResultDataAccessException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  /**
   * 게시글에 달린 댓글 목록 조회
   *
   * @param articleNum
   * @return 댓글 목록
   */
  @Override
  public List<Comment> findAll(Long articleNum) {
    StringBuffer sql = new StringBuffer();

    sql.append("select article_num, comment_group, comment_num, p_comment_num, step, comment_order, ");
    sql.append("p_comment_nickname, m.mem_nickname, comment_contents, create_date, reply ");
    sql.append("from comments c, member m ");
    sql.append("where c.mem_number = m.mem_number and c.article_num = ? ");
    sql.append("order by comment_group asc, comment_order asc ");

    List<Comment> comments = jt.query(sql.toString(), new RowMapper<Comment>() {
      @Override
      public Comment mapRow(ResultSet rs, int rowNum) throws SQLException {
        Member member = (new BeanPropertyRowMapper<>(Member.class)).mapRow(rs, rowNum);
        Comment comment = (new BeanPropertyRowMapper<>(Comment.class)).mapRow(rs, rowNum);
        comment.setMember(member);
        return comment;
      }
    },articleNum);

    return comments;
  }

  /**
   * 댓글 작성
   *
   * @param comment 댓글 정보
   * @return 작성된 댓글 수
   */
  @Override
  public int save(Comment comment) {

    //답댓글이라면
    if (comment.getPCommentNum() != null) {
      log.info("댓글번호 : {}",comment.getPCommentNum());
      Optional<Comment> parentComment = find(comment.getPCommentNum());

      //답댓글 step = 부모 댓글 step + 1
      comment.setStep(parentComment.get().getStep() + 1);

//      //대댓글의 step = 그룹 내 최고 step
//      if (comment.getStep() == maxStep(comment)) {
//        comment.setCommentOrder(maxCommentOrder(comment) +1);
//        //대댓글의 step > 그룹 내 최고 step
//      } else if (comment.getStep() > maxStep(comment)) {
//        changeCommentOrder(parentComment.get());
//        comment.setCommentOrder(parentComment.get().getCommentOrder() + 1);
//        //대댓글의 step < 그룹 내 최고 step
//      } else {
//        changeCommentOrder(parentComment.get(),maxCommentOrderInSameParent(comment));
//        comment.setCommentOrder(maxCommentOrderInSameParent(comment) + 1);
//      }

//      if (maxCommentOrderInSameParent(comment) == maxCommentOrder(comment)) {
//        comment.setCommentOrder(maxCommentOrder(comment) + 1);
//      } else if (maxCommentOrderInSameParent(comment) < maxCommentOrder(comment)) {
//        changeCommentOrder(comment,maxCommentOrderInSameParent(comment));
//        comment.setCommentOrder(maxCommentOrderInSameParent(comment) + 1);
//      } else if (maxCommentOrderInSameParent(comment) == null) {
//        changeCommentOrder(parentComment.get());
//        comment.setCommentOrder(parentComment.get().getCommentOrder() + 1);
//      }

      if (maxCommentOrderInSameParent(comment) == null) {
        changeCommentOrder(parentComment.get());
        comment.setCommentOrder(parentComment.get().getCommentOrder() + 1);
      } else if (maxCommentOrderInSameParent(comment) == maxCommentOrder(comment)) {
        comment.setCommentOrder(maxCommentOrder(comment) + 1);
      } else if (maxCommentOrderInSameParent(comment) < maxCommentOrder(comment)) {
        changeCommentOrder(comment,maxCommentOrderInSameParent(comment));
        comment.setCommentOrder(maxCommentOrderInSameParent(comment) + 1);
      }
    }



    StringBuffer sql = new StringBuffer();

    sql.append("insert into comments ");
    sql.append("(article_num, comment_group, comment_num, p_comment_num, step, comment_order, ");
    sql.append("p_comment_nickname, mem_number, comment_contents, create_date, reply) ");
    sql.append("values (?,?,?,?,?,?,?,?,?,sysdate, ?) ");

    log.info("쿼리 삽입하기 전 코멘트 내용 : {}", comment);


    int affectedRow = jt.update(sql.toString(),
        comment.getArticleNum(),
        comment.getCommentGroup(),
        comment.getCommentNum(),
        comment.getPCommentNum(),
        comment.getStep(),
        comment.getCommentOrder(),
        comment.getPCommentNickname(),
        comment.getMemNumber(),
        comment.getCommentContents(),
        comment.getReply());

    return affectedRow;
  }

  //부모 댓글과 동일한 그룹의 답댓글들의 step 변경
  private int updateStep(Comment comment){
    StringBuffer sql = new StringBuffer();

    sql.append("update comments ");
    sql.append("set step = step + 1 ");
    sql.append("where comment_group = ? ");
    sql.append("and step > ? ");
    sql.append("and article_num = ? ");

    int affectedRows = jt.update(sql.toString(), comment.getCommentGroup(), comment.getStep(),comment.getArticleNum());

    return affectedRows;
  }

  //댓글 그룹 내 최고 step 산출
  private Long maxStep(Comment comment){
    String sql = "select max(step) from comments where article_num = ? and comment_group = ? ";

    Long maxStep = jt.queryForObject(sql, Long.class, comment.getArticleNum(), comment.getCommentGroup());

    return maxStep;
  }

  //그룹 내 댓글 순서 최댓값 산출
  private Long maxCommentOrder(Comment comment) {
    String sql = "select max(comment_order) from comments where article_num = ? and comment_group = ? ";

    Long maxCommentOrder = jt.queryForObject(sql, Long.class,comment.getArticleNum(),comment.getCommentGroup());

    return maxCommentOrder;
  }

  //같은 부모 댓글을 가진 동일 step내 순서 최댓값 산출
  private Long maxCommentOrderInSameParent (Comment comment) {
    StringBuffer sql = new StringBuffer();
    sql.append("select max(comment_order) from comments ");
    sql.append("where article_num = ? and comment_group = ? and comment_num = ? and step = ? ");

    Long maxCommentOrderInSameParent = jt.queryForObject(sql.toString(), Long.class, comment.getArticleNum(), comment.getCommentGroup(),
                                                                                     comment.getPCommentNum(), comment.getStep());

//    if (maxCommentOrderInSameParent == null) {
//      return maxCommentOrder(comment);
//    }

      return maxCommentOrderInSameParent;

  }

  //댓글 순서 변경
  private void changeCommentOrder(Comment comment){
    StringBuffer sql = new StringBuffer();

    sql.append("update comments ");
    sql.append("set comment_order = comment_order + 1 ");
    sql.append("where article_num =? and comment_group = ? ");
    sql.append("and comment_order > ? ");

    jt.update(sql.toString(),comment.getArticleNum(), comment.getCommentGroup(),comment.getCommentOrder());
  }

  //댓글 순서 변경2
  private void changeCommentOrder(Comment comment, Long commentOrder){
    StringBuffer sql = new StringBuffer();

    sql.append("update comments ");
    sql.append("set comment_order = comment_order + 1 ");
    sql.append("where article_num =? and comment_group = ? ");
    sql.append("and comment_order > ? ");

    jt.update(sql.toString(),comment.getArticleNum(), comment.getCommentGroup(),commentOrder);
  }



  /**
   * 댓글 수정
   *
   * @param commentNum 댓글 번호
   * @param comment    댓글 내용
   * @return
   */
  @Override
  public int update(Long commentNum, Comment comment) {
    StringBuffer sql = new StringBuffer();

    sql.append("update comments ");
    sql.append("set comment_contents = ?, create_date = sysdate ");
    sql.append("where comment_num = ? ");

    int affectedRow = jt.update(sql.toString(),
        comment.getCommentContents(),
        commentNum);

    return affectedRow;
  }

  /**
   * 댓글 삭제
   *
   * @param commentNum 댓글 번호
   * @return 댓글 내용
   */
  @Override
  public int delete(Long commentNum) {
    String sql = "delete from comments where comment_num = ? ";

    int affectedRow = jt.update(sql, commentNum);

    return affectedRow;
  }

  /**
   * 신규 댓글 번호 생성
   *
   * @return 댓글 번호
   */
  @Override
  public Long generatedCommentNum() {
    String sql = "select comments_comment_num_seq.nextval from dual ";
    Long commentNum = jt.queryForObject(sql, Long.class);
    return commentNum;
  }

  /**
   * 신규 댓글 그룹 번호 생성
   *
   * @return 댓글 그룹 번호
   */
  @Override
  public Long generatedCommentGroupNum() {
    String sql = "select comments_comment_num_seq.currval from dual ";
    Long commentGroupNum = jt.queryForObject(sql, Long.class);
    return commentGroupNum;
  }

  /**
   * 게시물 댓글 건수 조회
   *
   * @param articleNum 게시글 번호
   * @return 댓글 건수
   */
  @Override
  public int totalCountOfArticle(Long articleNum) {

    String sql = "select count(*) from comments where article_num = ? ";
    Integer cntPerArticle = jt.queryForObject(sql, Integer.class, articleNum);
    return cntPerArticle;
  }
}