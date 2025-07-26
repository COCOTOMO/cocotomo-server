package com.uthon.cocotomo.repository;

import com.uthon.cocotomo.entity.Diary;
import com.uthon.cocotomo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
    List<Diary> findByDateContains(String date);
    Optional<Diary> findByDateAndUser(String date, User user);
}