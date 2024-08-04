package com.tunapearl.saturi.service;

import com.tunapearl.saturi.domain.game.GameRoomEntity;
import com.tunapearl.saturi.domain.quiz.GameRoomQuizEntity;
import com.tunapearl.saturi.domain.quiz.QuizEntity;
import com.tunapearl.saturi.repository.QuizRepository;
import com.tunapearl.saturi.repository.game.GameRoomQuizRepository;
import com.tunapearl.saturi.repository.game.GameRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class GameRoomQuizService {

    private final GameRoomQuizRepository grQuizRepository;
    private final QuizRepository quizRepository;
    private final GameRoomRepository roomRepository;

    public void poseTenQuiz(Long roomId, List<Long> quisIdList){
        GameRoomEntity room = roomRepository.findById(roomId).orElseThrow(()
                -> new RuntimeException(String.format("존재하지 않는 게임방 ID 입니다: %d", roomId)));
        List<QuizEntity> quizList = quizRepository.findByIdList(quisIdList);

        for(int i = 0; i < quizList.size(); i++){
            GameRoomQuizEntity grQuiz = GameRoomQuizEntity.create(room, quizList.get(i), Long.valueOf(i+1));
            grQuizRepository.save(grQuiz);
        }
    }


}
