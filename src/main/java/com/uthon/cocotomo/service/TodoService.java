package com.uthon.cocotomo.service;

import com.uthon.cocotomo.dto.TodoItemsRequest;
import com.uthon.cocotomo.dto.TodoRequest;
import com.uthon.cocotomo.dto.TodoResponse;
import com.uthon.cocotomo.entity.Todo;
import com.uthon.cocotomo.entity.User;
import com.uthon.cocotomo.repository.TodoRepository;
import com.uthon.cocotomo.repository.UserRepository;
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
