package com.uthon.cocotomo.service;

import com.uthon.cocotomo.dto.AddDiaryRequest;
import com.uthon.cocotomo.dto.TodoItemsRequest;
import com.uthon.cocotomo.entity.Diary;
import com.uthon.cocotomo.entity.User;
import com.uthon.cocotomo.repository.DiaryRepository;
import com.uthon.cocotomo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository repository;
    private final UserRepository userRepository;

    public void save(AddDiaryRequest req) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Diary diary = new Diary();
        diary.setUser(user);
        diary.setContent(req.getContent());
        diary.setDate(req.getDate().toString());
        repository.save(diary);
    }

    public List<String> getByMonth(String date) {
        List<Diary> localDates = repository.findByDateContains(date);
        List<String> dates = new ArrayList<>();
        localDates.stream().map(d -> {
            return dates.add(d.getDate());
        }).toList();
        return dates;
    }
}
