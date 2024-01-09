package com.plate.silverplate.nutritionFact.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plate.silverplate.common.exception.ErrorCode;
import com.plate.silverplate.common.exception.ErrorException;
import com.plate.silverplate.nutritionFact.domain.entity.NutritionFact;
import com.plate.silverplate.nutritionFact.domain.repo.NutritionFactRepository;
import com.plate.silverplate.nutritionFact.dto.response.NutritionFactResponse;
import org.springframework.scheduling.annotation.EnableScheduling;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@EnableScheduling
public class NutritionFactService {

    @Value("${nutrition.key_id}")
    private String keyId;

    @Value("${nutrition.service_id}")
    private String serviceId;
    private final NutritionFactRepository nutritionFactRepository;

    private final RestTemplate restTemplate;
    // 우선 스케줄러와 데이터를 박아놓는 거 중에서 고민중
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
//    @PostConstruct
    public void listCreate(){
        Long countAll = nutritionFactRepository.countBy();
        for (int i = (int) (countAll/1000); i < 92; i++) {
            int startIdx = 1 + 1000*i;
            int endIdx = (i+1) *1000;
            createNutrition(startIdx,endIdx);


        }
    }

    /*
     * url에 있는 값 String 형태로 받아오는 메소드
     * */
    private String getNutrition(int startIdx, int endIdx){

        RestClient restClient = RestClient.create(restTemplate);

        return  restClient.get()
                .uri("http://openapi.foodsafetykorea.go.kr/api/{keyId}/{serviceId}/json/{startIdx}/{endIdx}" ,keyId , serviceId,startIdx,endIdx)
                .retrieve()
                .body(String.class);

    }



    /*
     * getNutrition 메소드를 통해 jsonString 값 받아오기
     * getListNutritionDto를 통해  List<NutritionFactResponse> 형변환
     * toListEntity를 통해 dto 리스트값들 entity값으로 변환
     * 변환된 엔티티를 저장하는 메소드
     * */
    @Transactional
    public void createNutrition(int startIdx, int endIdx){
        String response = getNutrition(startIdx, endIdx);
        List<NutritionFactResponse> responseList = getListNutritionDto(response);
        if( responseList == null || responseList.isEmpty() ){
            throw new ErrorException(ErrorCode.NON_EXISTENT_LIST_DTO);
        }
        List<NutritionFact> nutritionFacts = NutritionFactResponse.toListEntity(responseList);
        nutritionFactRepository.saveAll(nutritionFacts);
    }

    /*
     * jsonString 값을 list<NutritionFactResponse> 형 변환 메소드
     * ObjectMapper를 통해 json 코드 안에 row 데이터에 필요한 정보 불러옴
     * */
    public List<NutritionFactResponse> getListNutritionDto(String response) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode rows = root.at("/" + serviceId + "/row");
            String json = objectMapper.writeValueAsString(rows);

            return objectMapper.readValue(json, new TypeReference<List<NutritionFactResponse>>(){});
        } catch (JsonProcessingException e) {
            throw new ErrorException(ErrorCode.JSON_NOT_PROCESSING);
        }
    }

}