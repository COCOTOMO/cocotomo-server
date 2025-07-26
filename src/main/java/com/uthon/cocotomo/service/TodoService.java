package com.study.practice.service;

import com.study.practice.dto.TodoItemsRequest;
import com.study.practice.dto.TodoRequest;
import com.study.practice.dto.TodoResponse;
import com.study.practice.entity.Todo;
import com.study.practice.entity.User;
import com.study.practice.repository.TodoRepository;
import com.study.practice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TodoService {
    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    public void add(TodoRequest request) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Todo todo = new Todo();
        todo.setContent(request.getContent());
        todo.setCompleted(false);
        todo.setUser(user);
        todo.setDate(LocalDate.now());
        todoRepository.save(todo);
    }

    public void delete(Long id) {
        todoRepository.deleteById(id);
    }

    public List<TodoResponse> getItems(TodoItemsRequest request) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<Todo> todos = todoRepository.findAllByUserAndDate(user, request.getDate());
        return todos.stream()
            .map(todo -> {
                return new TodoResponse(
                    todo.getId(), todo.getContent(), todo.isCompleted()
                );
            }).toList();
    }
}
