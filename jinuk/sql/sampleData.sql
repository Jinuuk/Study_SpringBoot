--회원 임시 테이블 샘플 데이터 삽입
insert into member values (1, '닉네임1');
insert into member values (2, '닉네임2');
insert into member values (3, '닉네임3');
insert into member values (4, '닉네임4');

select article_article_num_seq.nextval from dual;
select article_article_num_seq.nextval from dual;
select article_article_num_seq.nextval from dual;
select article_article_num_seq.nextval from dual;
commit;

----판매 임시 테이블 샘플 데이터 삽입
--insert into product_info values (1);
--insert into product_info values (2);
--insert into product_info values (3);
--insert into product_info values (4);
--
----리뷰 임시 테이블 샘플 데이터 삽입
--insert into review values (1);
--insert into review values (2);
--insert into review values (3);
--insert into review values (4);
--
----공지 임시 테이블 샘플 데이터 삽입
--insert into notice values (1);
--insert into notice values (2);
--insert into notice values (3);
--insert into notice values (4);
--
----프로필 임시 테이블 샘플 데이터 삽입
--insert into profile values (1);
--insert into profile values (2);
--insert into profile values (3);
--insert into profile values (4);

--커뮤니티 테이블 샘플 데이터 삽입
insert into article values (1,1,'문의','글제목1','글내용1','Y',sysdate,0,0);
insert into article values (2,2,'남구','글제목2','글내용2','N',sysdate,0,0);
insert into article values (3,2,'북구','글제목3','글내용3','Y',sysdate,0,0);
insert into article values (4,4,'울주군','글제목4','글내용4','N',sysdate,0,0);
commit;

--댓글 테이블 샘플 데이터 삽입
insert into comments values (1,1,1,null,1,'댓글 내용1',sysdate);
insert into comments values (1,2,2,null,2,'댓글 내용2',sysdate);
insert into comments values (1,1,3,1,3,'댓글 내용3',sysdate);
insert into comments values (1,2,4,2,4,'댓글 내용4',sysdate);
select comments_comment_group_seq.nextval from dual;
select comments_comment_group_seq.nextval from dual;
select comments_comment_num_seq.nextval from dual;
commit;

----신고 테이블 샘플 데이터 삽입
--insert into report values (1,1,2,sysdate,1,'신고 내용1',null,1,null,null);
--insert into report values (2,3,2,sysdate,2,'신고 내용2',null,2,null,null);
--insert into report values (3,1,3,sysdate,3,'신고 내용3',null,3,null,null);
--insert into report values (4,2,4,sysdate,4,'신고 내용4',null,4,null,null);
--
----제재 테이블 샘플 데이터 삽입
--insert into penalty values (1,1,2,'제재 내용','2022-08-28');
--insert into penalty values (2,2,2,'제재 내용','2022-08-28');
--insert into penalty values (3,3,3,'제재 내용','2022-09-01');
--insert into penalty values (4,4,4,'제재 내용','2022-09-22');

commit;

--테이블 조회
select * from member;
--select * from product_info;
--select * from review;
--select * from notice;
--select * from profile;
select * from article;
select * from comments;
select * from uploadfile;
--select * from report;
--select * from penalty;