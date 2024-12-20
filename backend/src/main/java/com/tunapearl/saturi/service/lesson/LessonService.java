package com.tunapearl.saturi.service.lesson;

import com.tunapearl.saturi.domain.lesson.*;
import com.tunapearl.saturi.domain.user.UserEntity;
import com.tunapearl.saturi.dto.admin.lesson.LessonResponseDTO;
import com.tunapearl.saturi.dto.lesson.*;
import com.tunapearl.saturi.dto.user.UserExpInfoCurExpAndEarnExp;
import com.tunapearl.saturi.repository.UserRepository;
import com.tunapearl.saturi.repository.lesson.LessonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;

    // 상수 정의
    private static final Long FIRST_LEARN_LESSON_EXP = 20L;
    private static final Long REVIEW_LEARN_LESSON_EXP = 10L;

    public LessonCategoryEntity findByIdLessonCategory(Long lessonCategoryId) {
        return lessonRepository.findByIdLessonCategory(lessonCategoryId).orElse(null);
    }

    public List<LessonCategoryEntity> findAllLessonCategory() {
        return lessonRepository.findAllLessonCategory().orElse(null);
    }

    public LessonGroupEntity findByIdLessonGroup(Long lessonGroupId) {
        return lessonRepository.findByIdLessonGroup(lessonGroupId).orElse(null);
    }

    public List<LessonGroupEntity> findAllLessonGroup() {
        return lessonRepository.findAllLessonGroup().orElse(null);
    }



    public List<LessonGroupEntity> findLessonGroupByLocationAndCategory(Long locationId, Long categoryId) {
        return lessonRepository.findLessonGroupByLocationAndCategory(locationId, categoryId).orElse(null);
    }

    public LessonEntity findById(Long lessonId) {
        LessonEntity findLesson = lessonRepository.findById(lessonId).orElse(null);
        if(findLesson == null) throw new IllegalArgumentException("존재하지 않는 레슨입니다.");
        return findLesson;
    }

    public LessonResponseDTO createLessonDTO(LessonEntity findLesson) {
        return new LessonResponseDTO(findLesson.getLessonId(), findLesson.getLessonGroup().getLessonGroupId(),
                findLesson.getLessonGroup().getName(), findLesson.getSampleVoicePath(), findLesson.getSampleVoiceName(),
                findLesson.getScript(), findLesson.getGraphX(), findLesson.getGraphY(), findLesson.getLastUpdateDt());
    }

    public Long getProgressByUserIdLocationAndCategory(Long userId, Long locationId, Long lessonCategoryId) {
        // 유저 아이디로 그룹 결과 조회(지역, 카테고리 맞는거, 완료된거만)
        List<LessonGroupResultEntity> lessonGroupResult = lessonRepository.findLessonGroupResultByUserIdWhereLocationAndCategory(userId, locationId, lessonCategoryId).orElse(null);
        if(lessonGroupResult == null) return 0L;
        // 완료한 퍼즐 / 9개
        return (lessonGroupResult.size() * 100L) / 9L;
    }

    public List<LessonGroupProgressByUserDTO> getLessonGroupProgressAndAvgAccuracy(Long userId, Long locationId, Long lessonCategoryId) {
        // lessonGroup 완성 여부에 상관없이 lessonGroupResult 받아오기
        List<LessonGroupResultEntity> lessonGroupResult = lessonRepository.findLessonGroupResultByUserIdWithoutIsCompleted(userId, locationId, lessonCategoryId).orElse(null);
        List<LessonGroupEntity> lessonGroups = lessonRepository.findLessonGroupByLocationAndCategory(locationId, lessonCategoryId).orElse(null);
        List<LessonGroupProgressByUserDTO> result = new ArrayList<>();

        if(lessonGroups == null) throw new IllegalArgumentException("잘못된 지역이거나 학습 유형입니다.");

        if(lessonGroupResult == null) { // 아예 없으면
            for (LessonGroupEntity lg : lessonGroups) {
                LessonGroupProgressByUserDTO dto = new LessonGroupProgressByUserDTO(lg.getLessonGroupId(), lg.getName(), 0L, 0L);
                result.add(dto);
            }
            return result;
        }
        Set<Long> lessonGroupIdSet = new HashSet<>(); // front 요청으로 lessonGroupResult가 없어도 레슨그룹아이디, 레슨그룹이름을 dto에 추가

        // lessonGroupResult score 계산을 위해 lessonGroupResult를 맵으로 구성
        Map<Long, LessonGroupResultEntity> lessonGroupResultMap = new HashMap<>();

        // in 절 조회를 위한 id list
        List<Long> lessonGroupResultIdList = new ArrayList<>();

        // 빈 껍데기만 있는 레슨그룹결과(학습을 하지않거나 다 건너뛰었거나) 찾기용 set
        Map<Long, Boolean> lessonGroupResultNoLearn = new HashMap<>();

        for (LessonGroupResultEntity lgr : lessonGroupResult) {
            // id list에 추가
            lessonGroupResultIdList.add(lgr.getLessonGroupResultId());

            // lessonGroupId Set에 각 레슨 그룹 아이디 추가(유저가 학습하지 않은 레슨 그룹도 레슨 그룹의 정보를 반환)
            lessonGroupIdSet.add(lgr.getLessonGroup().getLessonGroupId());

            // 각 레슨 그룹 결과를 돌면서 레슨 결과를 조회하기 위함
            lessonGroupResultMap.put(lgr.getLessonGroupResultId(), lgr);

            // 빈 껍데기만 있는 레슨그룹결과(학습을 하지않거나 다 건너뛰었거나) 찾기용
            lessonGroupResultNoLearn.put(lgr.getLessonGroup().getLessonGroupId(), false);
        }

        // 퍼즐 9개의 결과 id로 조회한 lessonResult list
        List<LessonResultEntity> lessonResults = lessonRepository.findLessonResultByLessonGroupResultId(lessonGroupResultIdList).orElse(null);
        if(lessonResults == null) { // 레슨 그룹 결과 테이블 생성해놓고 레슨 학습 안하고 나갔을 때
            for (LessonGroupEntity lg : lessonGroups) {
                LessonGroupProgressByUserDTO dto = new LessonGroupProgressByUserDTO(lg.getLessonGroupId(), lg.getName(), 0L, 0L);
                result.add(dto);
            }
            return result;
        }

        // key = lessonGroupResultId, value = LessonResults
        // 각 레슨 그룹 결과 아이디에 매핑된 레슨결과들 저장(복습이나 건너뛰기로 레슨 결과가 여러 개 일 수 있어서 리스트로)
        Map<Long, List<LessonResultEntity>> lessonResultMap = new HashMap<>();

        // Map에 넣기
        for (LessonResultEntity lr : lessonResults) {
            Long lessonGroupResultId = lr.getLessonGroupResult().getLessonGroupResultId();
            // 빈 껍데기만 있는 레슨그룹결과(학습을 하지않거나 다 건너뛰었거나) 찾기용
            lessonGroupResultNoLearn.replace(lr.getLesson().getLessonGroup().getLessonGroupId(), true);
            if(lessonResultMap.containsKey(lessonGroupResultId)) {
                lessonResultMap.get(lessonGroupResultId).add(lr);
            } else {
                lessonResultMap.put(lessonGroupResultId, new ArrayList<>());
                lessonResultMap.get(lessonGroupResultId).add(lr);
            }
        }

        // 각 Map을 돌면서 결과 구하기
        for (Long lessonGroupResultId : lessonResultMap.keySet()) {
            // 이미 나왔던 lessonId는 거르기
            Set<Long> lessonIdSet = new HashSet<>();

            List<LessonResultEntity> lessonResultList = lessonResultMap.get(lessonGroupResultId);

            // 이미 나온 lessonId는 거르고 set에 넣기
            for (LessonResultEntity lr : lessonResultList) {
                if(lessonIdSet.contains(lr.getLesson().getLessonId())) continue;
                lessonIdSet.add(lr.getLesson().getLessonId());
            }
            // lessonGroup progress
            Long groupProcess = (lessonIdSet.size() * 100L) / 5L;

            // lessonGroup score
            LessonGroupResultEntity lgr = lessonGroupResultMap.get(lessonGroupResultId);
            Long avgAccuracy = 0L;
            if(lgr.getAvgAccuracy() == null || lgr.getAvgSimilarity() == null) {
                avgAccuracy = 0L;
            } else{
                avgAccuracy = (lgr.getAvgAccuracy() + lgr.getAvgSimilarity()) / 2L;
            }

            // DTO 변환
            LessonGroupProgressByUserDTO dto = new LessonGroupProgressByUserDTO(lgr.getLessonGroup().getLessonGroupId(), lgr.getLessonGroup().getName(), groupProcess, avgAccuracy);
            result.add(dto);
        }

        // 유저가 학습하지 않은 레슨 그룹은 progress, score를 0으로 한 후 result에 추가
        for (LessonGroupEntity lg : lessonGroups) {
            if(lessonGroupIdSet.contains(lg.getLessonGroupId())) {
                // 빈 껍데기만 있는 레슨그룹결과(학습을 하지않거나 다 건너뛰었거나) 찾기용
                if(lessonGroupResultNoLearn.get(lg.getLessonGroupId())) continue;
            }
            LessonGroupProgressByUserDTO dto = new LessonGroupProgressByUserDTO(lg.getLessonGroupId(), lg.getName(), 0L, 0L);
            result.add(dto);
        }
        // 유저가 학습하지 않은 레슨 그룹은 마지막에 추가하기 때문에 퍼즐의 순서가 섞임
        // lessonGroupId가 애초에 추가할 때 퍼즐 순서대로 넣어서 lessonGroupId로 정렬 -> 실제 운영단계일 때는 id 순서대로 안 되어있을 수도 있음
        result.sort(Comparator.comparing(LessonGroupProgressByUserDTO::getLessonGroupId));
        return result;
    }

    public Long skipLesson(Long userId, Long lessonId) {
        // 레슨아이디로 레슨그룹 아이디를 찾는다
        LessonEntity findLesson = lessonRepository.findById(lessonId).orElse(null);
        Long lessonGroupId = findLesson.getLessonGroup().getLessonGroupId();

        // 유저아이디와 레슨그룹 아이디로 레슨그룹결과 아이디를 찾는다.
        LessonGroupResultEntity lessonGroupResult = lessonRepository.findLessonGroupResultByUserIdAndLessonGroupId(userId, lessonGroupId).orElse(null);
        if(lessonGroupResult == null) {
            // 레슨그룹결과 없이 건너뛰기로 올 수 없음 -> 이상하게 온거
            throw new IllegalStateException("잘못된 접근입니다.");
        }
        Long lessonGroupResultId = lessonGroupResult.getLessonGroupResultId();

        // 레슨아이디와 레슨그룹결과아이디로 레슨결과를 생성한다. 이 때 isSkipped만 true로 해서 생성한다. (add) 학습 시간도 초기화
        // 이미 학습했던 레슨이면 기존에 존재하는 레슨 결과 아이디를 반환(현재는 ok 메시지만 반환)
        // 이미 학습한게 아닌 이전에 그냥 건너뛰기만 했어도 새로 일단 만들어줌
        Optional<List<LessonResultEntity>> lessonResultsOpt = lessonRepository.findLessonResultByLessonIdAndLessonGroupResultId(lessonId, lessonGroupResultId, false);
        if(lessonResultsOpt.isPresent()) {
            // 이미 레슨결과가 존재
            List<LessonResultEntity> lessonResults = lessonResultsOpt.get();
//            lessonResults.sort(Comparator.comparing(LessonResultEntity::getLessonDt).reversed());
            return lessonResults.get(lessonResults.size()-1).getLessonResultId(); // 최근 레슨 결과를 반환
        }

        //
        LessonResultEntity lessonResultSkipped = createLessonResultSkipped(findLesson, lessonGroupResult);

        // 생성한 레슨결과를 저장하고 레슨결과아이디를 리턴한다.
        return lessonRepository.saveLessonForSkipped(lessonResultSkipped).orElse(null);
    }

    private static LessonResultEntity createLessonResultSkipped(LessonEntity findLesson, LessonGroupResultEntity findLessonGroupResult) {
        LessonResultEntity lessonResultSkipped = new LessonResultEntity();
        lessonResultSkipped.setIsSkipped(true);
        lessonResultSkipped.setAccentSimilarity(0L);
        lessonResultSkipped.setPronunciationAccuracy(0L);
        lessonResultSkipped.setLesson(findLesson);
        lessonResultSkipped.setLessonDt(LocalDateTime.now());
        lessonResultSkipped.setLessonGroupResult(findLessonGroupResult);
        return lessonResultSkipped;
    }

    public Long createLessonGroupResult(Long userId, Long lessonGroupId) {
        // userId로 유저 조회
        UserEntity findUser = userRepository.findByUserId(userId).orElse(null);

        // lessonGroupId로 레슨 그룹 조회
        LessonGroupEntity findLessonGroup = lessonRepository.findByIdLessonGroup(lessonGroupId).orElse(null);

        // 이미 레슨그룹결과가 있는지 확인
        Optional<LessonGroupResultEntity> getLessonGroupResult = lessonRepository.findLessonGroupResultByUserIdAndLessonGroupId(userId, lessonGroupId);
        if(getLessonGroupResult.isPresent()) {
            getLessonGroupResult.get().setStartDt(LocalDateTime.now()); // 복습이면 시간 현재로 갱신
            return getLessonGroupResult.get().getLessonGroupResultId();
        }

        // 유저, 레슨그룹, 레슨 그룹 시작 일시 설정, 완료 여부 false
        LessonGroupResultEntity lessonGroupResult = createLessonGroupResult(findUser, findLessonGroup);
        return lessonRepository.createLessonGroupResult(lessonGroupResult).orElse(null);
    }

    private static LessonGroupResultEntity createLessonGroupResult(UserEntity findUser, LessonGroupEntity findLessonGroup) {
        LessonGroupResultEntity lessonGroupResult = new LessonGroupResultEntity();
        lessonGroupResult.setUser(findUser);
        lessonGroupResult.setLessonGroup(findLessonGroup);
        lessonGroupResult.setStartDt(LocalDateTime.now());
        lessonGroupResult.setIsCompleted(false);
        return lessonGroupResult;
    }

    public Optional<LessonInfoDTO> getLessonInfoForUser(Long userId, Long lessonId) {
        // 레슨 아이디로 레슨 조회해서 레슨 그룹 아이디 조회
        LessonEntity lesson = lessonRepository.findById(lessonId).orElse(null);
        Long lessonGroupId = lesson.getLessonGroup().getLessonGroupId();

        // 유저 아이디와 레슨 그룹 아이디로 레슨 그룹 결과 조회
        Optional<LessonGroupResultEntity> lessonGroupResult = lessonRepository.findLessonGroupResultByUserIdAndLessonGroupId(userId, lessonGroupId);
        if(lessonGroupResult.isEmpty()) return Optional.empty();
        Long lessonGroupResultId = lessonGroupResult.get().getLessonGroupResultId();

        // 레슨 아이디랑 레슨 그룹 결과 아이디로 레슨 결과 조회
        Optional<List<LessonResultEntity>> lessonResults = lessonRepository.findLessonResultByLessonIdAndLessonGroupResultId(lessonId, lessonGroupResultId);

        // 결과가 없으면 null 반환
        if(lessonResults.isEmpty()) return Optional.empty();

        // 평균 정확도가 높은 순으로 정렬
        Collections.sort(lessonResults.orElse(null), (o1, o2) -> Long.compare((o2.getAccentSimilarity() + o2.getPronunciationAccuracy()) / 2,
                                                                                    (o1.getAccentSimilarity() + o1.getPronunciationAccuracy()) / 2));
        // 평균 정확도가 높은 레슨결과를 가져옴
        LessonResultEntity lessonResult = lessonResults.orElse(null).get(0);

        // 결과가 있는데 건너뛰기 한거면 건너뛰기로 데이터 반환
        if(lessonResult.getIsSkipped()) {
            return Optional.ofNullable(new LessonInfoDTO(true, null, null));
        }
        return Optional.ofNullable(new LessonInfoDTO(false, lessonResult.getAccentSimilarity(), lessonResult.getPronunciationAccuracy()));
    }

    public Long saveLesson(LessonSaveRequestDTO request) {
        // 레슨 아이디로 레슨 객체 조회
        LessonEntity findLesson = lessonRepository.findById(request.getLessonId()).orElse(null);

        // 레슨그룹결과아이디로 레슨그룹결과 객체 조회
        LessonGroupResultEntity findLessonGroupResult = lessonRepository.findLessonGroupResultById(request.getLessonGroupResultId()).orElse(null);

        // LessonResultEntity 객체 생성
        LessonResultEntity lessonResult = createLessonResult(findLesson, findLessonGroupResult, request);

        // 녹음 파일 관련, 파형 관련 추가
        LessonRecordFileEntity lessonRecordFile = createLessonRecordFile(request, lessonResult);
        LessonRecordGraphEntity lessonRecordGraph = createLessonRecordGraph(request, lessonResult);

        Long lessonResultId = lessonRepository.saveLessonResult(lessonResult).orElse(null);
        lessonRepository.saveLessonRecordFile(lessonRecordFile).orElse(null);
        lessonRepository.saveLessonRecordGraph(lessonRecordGraph).orElse(null);
        return lessonResultId;
    }

    private LessonResultEntity createLessonResult(LessonEntity lesson, LessonGroupResultEntity lessonGroupResult, LessonSaveRequestDTO request) {
        LessonResultEntity lessonResult = new LessonResultEntity();
        lessonResult.setLesson(lesson);
        lessonResult.setLessonGroupResult(lessonGroupResult);
        lessonResult.setAccentSimilarity(request.getAccentSimilarity());
        lessonResult.setPronunciationAccuracy(request.getPronunciationAccuracy());
        lessonResult.setLessonDt(LocalDateTime.now());
        lessonResult.setIsSkipped(false);
        return lessonResult;
    }

    private LessonRecordFileEntity createLessonRecordFile(LessonSaveRequestDTO request, LessonResultEntity lessonResult) {
        LessonRecordFileEntity lessonRecordFile = new LessonRecordFileEntity();
        lessonRecordFile.setLessonResult(lessonResult);
        lessonRecordFile.setUserVoiceFileName(request.getFileName());
        lessonRecordFile.setUserVoiceFilePath(request.getFilePath());
        lessonRecordFile.setUserVoiceScript(request.getScript());
        return lessonRecordFile;
    }

    private LessonRecordGraphEntity createLessonRecordGraph(LessonSaveRequestDTO request, LessonResultEntity lessonResult) {
        LessonRecordGraphEntity lessonRecordGraph = new LessonRecordGraphEntity();
        lessonRecordGraph.setLessonResult(lessonResult);
        lessonRecordGraph.setGraphX(request.getGraphInfoX());
        lessonRecordGraph.setGraphY(request.getGraphInfoY());
        return lessonRecordGraph;
    }

    public Long saveClaim(Long userId, Long lessonId, String content) {
        LessonClaimEntity lessonClaim = new LessonClaimEntity();
        UserEntity user = userRepository.findByUserId(userId).orElse(null);
        LessonEntity lesson = lessonRepository.findById(lessonId).orElse(null);
        lessonClaim.setUser(user);
        lessonClaim.setLesson(lesson);
        lessonClaim.setContent(content);
        lessonClaim.setClaimDt(LocalDateTime.now());
        return lessonRepository.saveLessonClaim(lessonClaim).orElse(null);
    }

    public List<LessonClaimEntity> findAllLessonClaim() {
        List<LessonClaimEntity> lessonClaims = lessonRepository.findAllLessonClaim().orElse(null);
        return lessonClaims;
    }

    public List<LessonEntity> findAllByLessonGroupId(Long lessonGroupId) {
        return lessonRepository.findAllByLessonGroupId(lessonGroupId).orElse(null);

    }

    public LessonGroupResultSaveResponseDTO saveLessonGroupResult(Long userId, Long lessonGroupResultId) {
        // leesonGroupResult 조회
        LessonGroupResultEntity lessonGroupResult = lessonRepository.findLessonGroupResultById(lessonGroupResultId).orElse(null);
        if(lessonGroupResult == null) throw new IllegalArgumentException("lessonGroupResultId가 올바르지 않습니다");

        // 레슨그룹 학습 시작 시간을 기준으로 학습과 복습을 구분
        LocalDateTime lessonGroupResultStartDt = lessonGroupResult.getStartDt();

        // 240808 lessonEntity 조회 추가 (프론트 - 결과페이지에서 원본 음성 파형이 필요하다)
        List<LessonEntity> lessons = lessonRepository.findAllByLessonGroupResultId(lessonGroupResult.getLessonGroup().getLessonGroupId()).orElse(null);
        Map<Long, LessonEntity> lessonsMap = new HashMap<>();
        if(lessons != null) {
            for (LessonEntity lesson : lessons) {
                lessonsMap.put(lesson.getLessonId(), lesson);
            }
        }

        // 최근순으로 정렬된 레슨 결과 조회(건너뛰기 포함)
        Optional<List<LessonResultEntity>> lessonResultsOpt = lessonRepository.findLessonResultByLessonGroupResultIdSortedByRecentDt(lessonGroupResultId);
        if(lessonResultsOpt.isEmpty()) throw new IllegalStateException("잘못된 접근입니다.");
        List<LessonResultEntity> lessonResults = lessonResultsOpt.get();

        //// 필요한 map 정의 ////
        // 일단 이번에 학습한 얘들을 넣고, 저번에 했어서 이번에 건너뛰기한거는 저번에 한 결과를 넣음
        Map<Long, LessonResultEntity> lessonResultMap = new HashMap<>(); // lessonId : LessonResult
        Map<Long, Long> expMap = new HashMap<>(); // lessonId : exp
        Map<Long, Long> maxSimilarityMap = new HashMap<>(); // lessonId : similarity
        Map<Long, Long> maxAccuracyMap = new HashMap<>(); // lessonId : accuracy
        Map<Long, PriorityQueue<LessonResultEntity>> pqMap = new HashMap<>(); // lessonId : PQ(LessonResultEntity) -> 사용할 때 pq 초기화 필요

        for (LessonResultEntity lr : lessonResults) { // 레슨결과 하나씩 본다
            // 레슨 아이디와 학습한 시각(혹은 건너뛰기한 시각)
            Long lessonId = lr.getLesson().getLessonId();
            LocalDateTime lessonResultDt = lr.getLessonDt();

            // 이번에 학습한 레슨 결과다.
            if(lessonGroupResultStartDt.isBefore(lessonResultDt)) {
                if(lr.getIsSkipped()) { // 건너뛰기 한 레슨결과다
                    lessonResultMap.put(lessonId, lr);
                    expMap.put(lessonId, 0L);
                    maxSimilarityMap.put(lessonId, 0L);
                    maxAccuracyMap.put(lessonId, 0L);
                } else { // 건너뛰기 하지 않았다
                    lessonResultMap.put(lessonId, lr);
                    expMap.put(lessonId, FIRST_LEARN_LESSON_EXP);
                    maxSimilarityMap.put(lessonId, lr.getAccentSimilarity());
                    maxAccuracyMap.put(lessonId, lr.getPronunciationAccuracy());
                }
            }
            // 저번에 학습한 레슨 결과다. -> 처음 학습(복습이 아닌)한 결과는 여기 올 수 없다. 근데 이전에 건너뛰기한 결과는 올 수도 있다.
            else {
                // lessonResultMap에 조회가 된다 -> 이전에 학습한 결과가 있거나, 이전에 그냥 건너뛰기만 했던 결과가 있다.
                if(lessonResultMap.containsKey(lessonId)) {
                    // 현재 lessonResultMap에 들어가있는 결과와 이전에 학습한 결과의 점수를 비교
                    Long currentScore = (lessonResultMap.get(lessonId).getAccentSimilarity() + lessonResultMap.get(lessonId).getPronunciationAccuracy()) / 2;
                    Long prevScore = (lr.getAccentSimilarity() + lr.getPronunciationAccuracy()) / 2;
                    // 이번에 학습한 score가 더 높다
                    if(currentScore > prevScore) {
                        if(expMap.get(lessonId).equals(0L)) { // 경험치가 0exp 이면
                            // 이미 복습을 실패했다. 그리고 score에도 이미 더 높은 수치가 들어가있다.
                            continue;
                        }
                        // 근데 이전에 건너뛰기를 한거였다면 경험치 유지
                        if(lr.getIsSkipped()) continue;
                        // 복습이면 경험치 조정
                        expMap.replace(lessonId, REVIEW_LEARN_LESSON_EXP);
                    }
                    // 이전에 학습한 score가 더 높다
                    else {
                        expMap.replace(lessonId, 0L); // 복습 실패
                        // 지금까지 scoreMap에 저장된 최댓값을 가져와서 비교한다.
                        Long prevMaxScore = (maxAccuracyMap.get(lessonId) + maxSimilarityMap.get(lessonId)) / 2;

                        // scoreMap을 더 높은 값으로 갱신
                        if(prevScore > prevMaxScore) { // max로 되어있는 score보다 더 높은게 나오면
                            maxSimilarityMap.replace(lessonId, lr.getAccentSimilarity());
                            maxAccuracyMap.replace(lessonId, lr.getPronunciationAccuracy());
                        }
                    }
                }
                // lessonResultMap에 조회가 안된다 -> 사용자가 이전에 학습했어서 건너뛰기 했다.(건너뛰기했는데 이전에 학습했었으면 건너뛰기 결과는 생성안돼서)
                else {
                    // pq에 추가해서 lessonResultMap에 이전에 학습한 결과를 넣어준다
                    if(pqMap.containsKey(lessonId)) { // pq에 이미 추가된 레슨결과가 있으면
                        pqMap.get(lessonId).offer(lr); // lessonResult 넣기
                    } else { // pq에 추가된 레슨결과가 없으면(pq 초기화 필요)
                        // pq를 초기화
                        pqMap.put(lessonId, new PriorityQueue<>((o1, o2) -> {
                            Long o1Score = (o1.getAccentSimilarity() + o1.getPronunciationAccuracy()) / 2;
                            Long o2Score = (o2.getAccentSimilarity() + o2.getPronunciationAccuracy()) / 2;
                            return Long.compare(o2Score, o1Score); // 내림차순
                        }));
                        pqMap.get(lessonId).offer(lr); // lessonResult 넣기
                    }
                }
            }
        } // end for lessonResult

        // isBeforeResult 설정을 위해 DTO를 미리 만들자.
        List<LessonResultForSaveGroupResultDTO> lessonResultDtoLst = new ArrayList<>();
        for (Long lessonId : lessonResultMap.keySet()) {
            LessonResultForSaveGroupResultDTO lessonResultDto = new LessonResultForSaveGroupResultDTO(lessonResultMap.get(lessonId), false, lessonsMap.get(lessonId));
            lessonResultDtoLst.add(lessonResultDto);
        }

        // pq에 값이 있는지 확인
        for (Long lessonId : pqMap.keySet()) {
            // pq의 첫번째 값을 lessonResultMap에 넣는다.(이전에 학습했던 결과 중 가장 잘한거)
            LessonResultEntity first = pqMap.get(lessonId).poll();
            lessonResultMap.put(first.getLesson().getLessonId(), first);

            // (추가) 레슨그룹결과 갱신 및 완료처리를 위해 넣어줌
            maxAccuracyMap.put(lessonId, first.getAccentSimilarity());
            maxSimilarityMap.put(lessonId, first.getPronunciationAccuracy());

            // isBeforeResult를 true로 해서 dto 생성
            LessonResultForSaveGroupResultDTO lessonResultDto = new LessonResultForSaveGroupResultDTO(lessonResultMap.get(lessonId), true, lessonsMap.get(lessonId));
            lessonResultDtoLst.add(lessonResultDto);
        }

        // lessonGroupResult를 갱신하자
        int lessonResultCnt = 0;
        Long sumSimilarity = 0L;
        Long sumAccuracy = 0L;
        for (Long lessonId : maxAccuracyMap.keySet()) {
            if(maxAccuracyMap.get(lessonId).equals(0L)) continue;
            lessonResultCnt++;
            sumSimilarity += maxSimilarityMap.get(lessonId); // 누적
            sumAccuracy += maxAccuracyMap.get(lessonId); // 누적
        }
        Long avgSimilarity; // 평균
        Long avgAccuracy; // 평균
        if(lessonResultCnt != 0) {
            avgSimilarity = sumSimilarity / lessonResultCnt; // 평균
            avgAccuracy = sumAccuracy / lessonResultCnt; // 평균
        } else {
            avgSimilarity = 0L;
            avgAccuracy = 0L;
        }

        lessonGroupResult.setAvgSimilarity(avgSimilarity); // score 갱신
        lessonGroupResult.setAvgAccuracy(avgAccuracy); // score 갱신
        if(lessonResultCnt == 5) { // 건너뛰기한게 없으면
            lessonGroupResult.setEndDt(LocalDateTime.now());
            lessonGroupResult.setIsCompleted(true);
        }

        // 경험치를 부여하자
        Long sumExp = 0L;
        for (Long lessonId : expMap.keySet()) {
            sumExp += expMap.get(lessonId);
        }
        UserEntity user = userRepository.findByUserId(userId).orElse(null);
        Long prevUserExp = user.getExp();
        user.setExp(prevUserExp + sumExp);
        Long curUserExp = user.getExp();

        // DTO를 만들자
        UserExpInfoCurExpAndEarnExp userExpDto = new UserExpInfoCurExpAndEarnExp(prevUserExp, sumExp, curUserExp);
        LessonGroupResultForSaveLessonGroupDTO lessonGroupResultForSaveLessonGroup = new LessonGroupResultForSaveLessonGroupDTO(lessonGroupResult);

        // lessonResultDtoLst 정렬시키기(레슨 순서 유지)
        lessonResultDtoLst.sort(Comparator.comparingLong(LessonResultForSaveGroupResultDTO::getLessonId));

        return new LessonGroupResultSaveResponseDTO(userExpDto, lessonResultDtoLst, lessonGroupResultForSaveLessonGroup);
    }

    public List<LessonGroupResultEntity> findLessonGroupResultWithoutIsCompletedAllByUserId(Long userId) {
        return lessonRepository.findLessonGroupResultByUserIdWithoutIsCompleted(userId).orElse(null);
    }

    public List<LessonResultEntity> findLessonResultByLessonGroupResultId(List<Long> lessonGroupResultIds) {
        return lessonRepository.findLessonResultByLessonGroupResultIdList(lessonGroupResultIds).orElse(null);
    }
}
