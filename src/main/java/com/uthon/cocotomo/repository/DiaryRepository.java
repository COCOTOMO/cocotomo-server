package com.uthon.cocotomo.repository;

import com.uthon.cocotomo.entity.Diary;
import com.uthon.cocotomo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
    List<Diary> findByDateContains(String date);
    Optional<Diary> findByDateAndUser(String date, User user);
    
    @Query(value = "SELECT * FROM diaries d WHERE d.date >= :sevenDaysAgo AND d.fk_user_id != :userId AND d.id NOT IN :commentedDiaryIds ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Diary> findRandomDiaryFromLastWeekExcludingUserAndCommented(
        @Param("sevenDaysAgo") String sevenDaysAgo, 
        @Param("userId") Long userId, 
        @Param("commentedDiaryIds") List<Long> commentedDiaryIds
    );
    
    @Query(value = "SELECT * FROM diaries d WHERE d.date >= :sevenDaysAgo AND d.fk_user_id != :userId ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Diary> findRandomDiaryFromLastWeekExcludingUser(
        @Param("sevenDaysAgo") String sevenDaysAgo, 
        @Param("userId") Long userId
    );
}