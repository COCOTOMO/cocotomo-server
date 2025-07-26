package com.uthon.cocotomo.repository;

import com.uthon.cocotomo.entity.Todo;
import com.uthon.cocotomo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findAllByUserAndDate(User user, LocalDate date);
}