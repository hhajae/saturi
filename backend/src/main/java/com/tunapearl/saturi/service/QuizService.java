package com.tunapearl.saturi.service;

import com.tunapearl.saturi.domain.LocationEntity;
import com.tunapearl.saturi.domain.quiz.QuizChoiceEntity;
import com.tunapearl.saturi.domain.quiz.QuizEntity;
import com.tunapearl.saturi.dto.admin.quiz.QuizRegisterRequestDto;
import com.tunapearl.saturi.dto.quiz.QuizReadRequestDto;
import com.tunapearl.saturi.dto.quiz.QuizReadResponseDto;
import com.tunapearl.saturi.repository.LocationRepository;
import com.tunapearl.saturi.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final LocationRepository locationRepository;

    public Long saveQuiz(QuizRegisterRequestDto registerRequestDto){
        LocationEntity location = locationRepository.findById(registerRequestDto.getLocationId()).orElseThrow();
        QuizEntity quiz = QuizEntity.createQuiz(
                location,
                registerRequestDto.getQuestion(),
                registerRequestDto.getIsObjective(),
                registerRequestDto.getChoiceList()
        );
        quizRepository.save(quiz);
        return quiz.getQuizId();
    }

    public List<QuizReadResponseDto> finaAll(QuizReadRequestDto quizReadRequestDto){
        List<QuizEntity> list = quizRepository.findAll(quizReadRequestDto);
        return list.stream().map(this::convertReadDtoToEntty).collect(Collectors.toList());
    }

    private QuizReadResponseDto convertReadDtoToEntty(QuizEntity quizEntity){
        return QuizReadResponseDto.builder()
                .quizId(quizEntity.getQuizId())
                .locationId(quizEntity.getLocation().getLocationId())
                .question(quizEntity.getQuestion())
                .creationDt(quizEntity.getCreationDt())
                .isObjective(quizEntity.getIsObjective())
                .build();
    }
}
