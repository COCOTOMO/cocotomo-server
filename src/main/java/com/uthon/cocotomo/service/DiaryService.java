package com.uthon.cocotomo.service;

import com.uthon.cocotomo.dto.AddDiaryRequest;
import com.uthon.cocotomo.entity.Diary;
import com.uthon.cocotomo.entity.User;
import com.uthon.cocotomo.exception.DiaryNotFoundException;
import com.uthon.cocotomo.exception.UserNotFoundException;
import com.uthon.cocotomo.repository.DiaryRepository;
import com.uthon.cocotomo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DiaryService {
    private final DiaryRepository repository;
    private final UserRepository userRepository;

    public void save(AddDiaryRequest req) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));
        Diary diary = new Diary();
        diary.setUser(user);
        diary.setContent(req.getContent());
        diary.setDate(req.getDate().toString());
        repository.save(diary);
    }

    public void update(String date, AddDiaryRequest req) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));
        Optional<Diary> optionalDiary = repository.findByDateAndUser(date, user);
        if (optionalDiary.isPresent()) {
            Diary diary = optionalDiary.get();
            diary.setContent(req.getContent());
            repository.save(diary);
        } else {
            throw new DiaryNotFoundException("일기를 찾을 수 없습니다");
        }
    }

    public List<String> getByMonth(String date) {
        List<Diary> localDates = repository.findByDateContains(date);
        List<String> dates = new ArrayList<>();
        localDates.stream().map(d -> {
            return dates.add(d.getDate());
        }).toList();
        return dates;
    }

    public Object getByDate(String date) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));
        return repository.findByDateAndUser(date, user).orElse(null);
    }
}
