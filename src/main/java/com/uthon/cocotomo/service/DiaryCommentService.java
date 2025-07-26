package com.uthon.cocotomo.service;

import com.uthon.cocotomo.dto.DiaryCommentRequest;
import com.uthon.cocotomo.dto.DiaryCommentResponse;
import com.uthon.cocotomo.dto.RandomDiaryResponse;
import com.uthon.cocotomo.entity.Diary;
import com.uthon.cocotomo.entity.DiaryComment;
import com.uthon.cocotomo.entity.User;
import com.uthon.cocotomo.exception.CommentNotAllowedException;
import com.uthon.cocotomo.exception.DiaryNotFoundException;
import com.uthon.cocotomo.exception.DuplicateCommentException;
import com.uthon.cocotomo.exception.NoRecommendedDiaryException;
import com.uthon.cocotomo.exception.UserNotFoundException;
import com.uthon.cocotomo.repository.DiaryCommentRepository;
import com.uthon.cocotomo.repository.DiaryRepository;
import com.uthon.cocotomo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryCommentService {
    private final DiaryRepository diaryRepository;
    private final DiaryCommentRepository commentRepository;
    private final UserRepository userRepository;

    public RandomDiaryResponse getRandomDiary() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));

        String sevenDaysAgo = LocalDate.now().minusDays(7).toString();
        List<Long> commentedDiaryIds = commentRepository.findCommentedDiaryIdsByCommenter(user);
        
        Optional<Diary> randomDiary;
        if (commentedDiaryIds.isEmpty()) randomDiary = diaryRepository.findRandomDiaryFromLastWeekExcludingUser(sevenDaysAgo, user.getId());
        else randomDiary = diaryRepository.findRandomDiaryFromLastWeekExcludingUserAndCommented(sevenDaysAgo, user.getId(), commentedDiaryIds);

        if (randomDiary.isEmpty()) {
            throw new NoRecommendedDiaryException("추천할 일기가 없습니다");
        }

        Diary diary = randomDiary.get();
        RandomDiaryResponse response = new RandomDiaryResponse();
        response.setId(diary.getId());
        response.setContent(diary.getContent());
        response.setDate(diary.getDate());
        return response;
    }

    public void addComment(Long diaryId, DiaryCommentRequest request) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User commenter = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryNotFoundException("일기를 찾을 수 없습니다"));

        if (diary.getUser().getId().equals(commenter.getId())) {
            throw new CommentNotAllowedException("자신의 일기에는 댓글을 달 수 없습니다");
        }
        if (commentRepository.existsByDiaryAndCommenter(diary, commenter)) {
            throw new DuplicateCommentException("이미 이 일기에 댓글을 작성했습니다");
        }

        DiaryComment comment = new DiaryComment();
        comment.setDiary(diary);
        comment.setCommenter(commenter);
        comment.setContent(request.getContent());
        commentRepository.save(comment);
    }

    public List<DiaryCommentResponse> getCommentsForMyDiaries() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));

        return commentRepository.findAll().stream()
                .filter(comment -> comment.getDiary().getUser().getId().equals(user.getId()))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<DiaryCommentResponse> getMyComments() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));

        return commentRepository.findByCommenter(user).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private DiaryCommentResponse convertToResponse(DiaryComment comment) {
        DiaryCommentResponse response = new DiaryCommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setCreatedAt(comment.getCreatedAt());
        response.setCommenterEmail(comment.getCommenter().getEmail());
        response.setDiaryId(comment.getDiary().getId());
        response.setDiaryDate(comment.getDiary().getDate());
        return response;
    }
}