package com.tunapearl.saturi.controller;

import com.tunapearl.saturi.domain.lesson.LessonCategoryEntity;
import com.tunapearl.saturi.domain.lesson.LessonEntity;
import com.tunapearl.saturi.domain.lesson.LessonGroupEntity;
import com.tunapearl.saturi.dto.admin.lesson.LessonGroupResponseDTO;
import com.tunapearl.saturi.dto.admin.lesson.LessonResponseDTO;
import com.tunapearl.saturi.dto.lesson.*;
import com.tunapearl.saturi.dto.user.UserExpAndRankDTO;
import com.tunapearl.saturi.dto.user.UserInfoResponseDTO;
import com.tunapearl.saturi.exception.UnAuthorizedException;
import com.tunapearl.saturi.service.lesson.LessonService;
import com.tunapearl.saturi.service.user.UserService;
import com.tunapearl.saturi.utils.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/learn")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class LessonController {

    private final LessonService lessonService;
    private final UserService userService;
    private final JWTUtil jwtUtil;

    /**
     * 모든 카테고리 조회
     */
    @GetMapping("/lesson-category")
    public ResponseEntity<List<LessonCategoryEntity>> getAllLessonCategory() {
        log.info("received request to get all lesson category");
        return ResponseEntity.ok(lessonService.findAllLessonCategory());
    }
    
    /**
     * 퍼즐 조회(지역+유형)
     * 레슨 그룹, 그룹안에 들어가있는 레슨들 정보도 같이 보냄
     */
    @GetMapping("/lesson-group")
    public ResponseEntity<List<LessonGroupResponseDTO>> getLessonGroupIdByLocationAndCategory(@ModelAttribute LocationIdAndCategoryIdDTO request) {
        log.info("received request to get lesson group id by location and category {}, {}", request.getLocationId(), request.getCategoryId());
        List<LessonGroupEntity> lessonGroupByLocationAndCategory = lessonService.findLessonGroupByLocationAndCategory(request.getLocationId(), request.getCategoryId());
        List<LessonGroupResponseDTO> result = lessonGroupByLocationAndCategory.stream()
                .map(g -> new LessonGroupResponseDTO(g)).toList();
        return ResponseEntity.ok(result);
    }

    /**
     * 레슨 개별 조회
     */
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<LessonResponseDTO> getLesson(@PathVariable Long lessonId) {
        log.info("received request to find Lesson {}", lessonId);
        LessonEntity findLesson = lessonService.findById(lessonId);
        return ResponseEntity.ok(new LessonResponseDTO(findLesson.getLessonId(),
                findLesson.getLessonGroup().getLessonGroupId(), findLesson.getLessonGroup().getName(),
                findLesson.getSampleVoicePath(), findLesson.getSampleVoiceName(), findLesson.getScript(), findLesson.getLastUpdateDt()));
    }

    /**
     * 현재 지역과 유형에 맞는 퍼즐의 유저별 정보 조회                                                
     * 진척도, 퍼즐별(진행률, 평균 정확도), 유저 정보(경험치, 순위)
     */
    @GetMapping("/lesson-group/progress")
    public ResponseEntity<LessonGroupProgressResponseDTO> getLessonGroupProgressByUser(@RequestHeader("Authorization") String authorization,
                                                                                       @RequestParam("locationId") Long locationId,
                                                                                       @RequestParam("categoryId") Long lessonCategoryId) throws UnAuthorizedException {
        log.info("received request to get lessonGroup progress by user {}, {}", locationId, lessonCategoryId);
        Long userId = jwtUtil.getUserId(authorization);

        // 진척도
        Long resultProgress = lessonService.getProgressByUserIdLocationAndCategory(userId, locationId, lessonCategoryId);

        // 퍼즐별 진행률, 평균 정확도(유사도+정확도/2)
        List<LessonGroupProgressByUserDTO> resultLessonGroupProgressAndAvgAccuracy = lessonService.getLessonGroupProgressAndAvgAccuracy(userId, locationId, lessonCategoryId);

        // 유저 정보
        UserInfoResponseDTO userProfile = userService.getUserProfile(userId);
        Long userRank = userService.getUserRank(userId);
        UserExpAndRankDTO resultUserInfo = new UserExpAndRankDTO(userProfile.getExp(), userRank);

        return ResponseEntity.ok(new LessonGroupProgressResponseDTO(resultProgress, resultLessonGroupProgressAndAvgAccuracy, resultUserInfo));
    }

    /**
     * 레슨 건너뛰기
     */
    @PutMapping("/lesson/{lessonId}")
    public ResponseEntity<LessonMsgResponseDTO> skipLesson(@RequestHeader("Authorization") String accessToken,
                                                           @PathVariable("lessonId") Long lessonId) throws UnAuthorizedException {
        //FIXME 이미 학습했던 레슨이라면 무시하도록 변경
        log.info("received request to skip Lesson {}", lessonId);
        Long userId = jwtUtil.getUserId(accessToken);
        Long lessonResultId = lessonService.skipLesson(userId, lessonId);
        return ResponseEntity.ok(new LessonMsgResponseDTO("ok"));
    }

    /**
     * 레슨 그룹 결과 테이블 생성
     * 이미 생성돼있으면 생성된 그룹 결과 id를 반환함
     */
    @PostMapping("/lesson-group-result/{lessonGroupId}")
    public ResponseEntity<CreateLessonGroupResultResponseDTO> createLessonGroupResult(@RequestHeader("Authorization") String accessToken,
                                                                                      @PathVariable("lessonGroupId") Long lessonGroupId) throws UnAuthorizedException {
        log.info("received request to create Lesson Group Result {}", lessonGroupId);
        Long userId = jwtUtil.getUserId(accessToken);
        Long lessonGroupResultId = lessonService.createLessonGroupResult(userId, lessonGroupId);
        return ResponseEntity.created(URI.create("/learn/lesson-group-result")).body(new CreateLessonGroupResultResponseDTO(lessonGroupResultId));
    }

    /**
     * 유저별 레슨 결과 조회
     */
    @GetMapping("lesson/user/{lessonId}")
    public ResponseEntity<LessonResultByUserResponseDTO> findUserLessonResult(@RequestHeader("Authorization") String accessToken,
                                                                              @PathVariable("lessonId") Long lessonId) throws UnAuthorizedException {
        log.info("received request to find user lesson result {}", lessonId);
        Long userId = jwtUtil.getUserId(accessToken);
        Boolean isAccessed = false;
        Optional<LessonInfoDTO> lessonInfo = lessonService.getLessonInfoForUser(userId, lessonId);
        if(lessonInfo.isPresent()) isAccessed = true;
        return ResponseEntity.ok(new LessonResultByUserResponseDTO(isAccessed, lessonInfo.orElse(null)));
    }

    /**
     * 레슨 저장
     */
    @PostMapping("lesson")
    public ResponseEntity<LessonMsgResponseDTO> saveLessonResult(@RequestHeader("Authorization") String accessToken,
                                                                 @RequestBody LessonSaveRequestDTO request) throws UnAuthorizedException {

        log.info("received request to save lesson result {}", request.getLessonId());
        Long userId = jwtUtil.getUserId(accessToken);
        Long savelessonId = lessonService.saveLesson(request);
        return ResponseEntity.created(URI.create("/learn/lesson")).body(new LessonMsgResponseDTO("ok"));
    }

    /**
     * 레슨 그룹 저장(레슨 다 학습한 뒤)
     * 이름이 저장이라서 헷갈리는데, 프론트에서 보냈던 레슨 저장 정보를 바탕으로 결과를 종합해서 리턴
     */
    @PutMapping("lesson-group-result/{lessonGroupResultId}")
    public ResponseEntity<LessonGroupResultSaveResponseDTO> saveLessonGroupResult(@RequestHeader("Authorization") String accessToken,
                                                   @PathVariable("lessonGroupResultId") Long lessonGroupResultId) throws UnAuthorizedException {
        log.info("received request to save lesson group result");
        // TODO 레슨 그룹 저장 기능 구현
        // TODO 경험치 부여, 평균 유사도, 평균 정확도 설정
        // TODO 5개 다 완료했으면 레슨 그룹 종료 일시 설정, 레슨 그룹 완료 여부 true로 변경
        // TODO 출력에 경험치 얼마 줬는지 보내야함
        // TODO 복습한 레슨 결과라면 경험치 줄이기??
        Long userId = jwtUtil.getUserId(accessToken);
        /**
         * UserInfo
         */
        // 유저의 현재 경험치 받아오기
        UserInfoResponseDTO user = userService.getUserProfile(userId);
        Long currentExp = user.getExp();

        // 유저가 획득한 경험치 받아오기(밑에 그룹결과 보고 판단)

        // 경험치 부여된 후 경험치(위에꺼 두개 더하거나 받아오기)

        /**
         * lessonResult
         */
        lessonService.saveLessonGroupResult(userId, lessonGroupResultId);

        /**
         * lessonGroupResult
         */

        return ResponseEntity.ok(new LessonGroupResultSaveResponseDTO());
    }

    /**
     * 레슨 신고
     */
    @PostMapping("/lesson/claim")
    public ResponseEntity<LessonMsgResponseDTO> claimLesson(@RequestHeader("Authorization") String accessToken,
                                                            @RequestBody LessonClaimRequestDTO request) throws UnAuthorizedException {
        log.info("received request to claim lesson {}", request.getLessonId());
        Long userId = jwtUtil.getUserId(accessToken);
        Long lessonClaimId = lessonService.saveClaim(userId, request.getLessonId(), request.getContent());
        return ResponseEntity.created(URI.create("/learn/lesson/claim")).body(new LessonMsgResponseDTO("ok"));
    }
}
