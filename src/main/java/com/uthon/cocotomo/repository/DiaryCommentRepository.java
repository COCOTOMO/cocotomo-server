package com.uthon.cocotomo.repository;

import com.uthon.cocotomo.entity.DiaryComment;
import com.uthon.cocotomo.entity.Diary;
import com.uthon.cocotomo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaryCommentRepository extends JpaRepository<DiaryComment, Long> {
    
    List<DiaryComment> findByDiary(Diary diary);
    
    List<DiaryComment> findByCommenter(User commenter);
    
    @Query("SELECT dc.diary.id FROM DiaryComment dc WHERE dc.commenter = :commenter")
    List<Long> findCommentedDiaryIdsByCommenter(@Param("commenter") User commenter);
    
    boolean existsByDiaryAndCommenter(Diary diary, User commenter);
}