package com.toy.core.domain.performance.dto;

import com.toy.core.domain.performance.Concert;
import com.toy.core.domain.performance.Performance;
import com.toy.core.domain.performance.Sports;
import lombok.Getter;

@Getter
public class PerformanceResponse {

    private Long id;
    private String type;  // "CONCERT" or "SPORTS"
    private String title;
    
    // 상세 정보 (값이 없으면 null)
    private String description; // 가수 이름이나 팀 대진 정보를 퉁쳐서 보여줄 필드

    public PerformanceResponse(Performance entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();

        // 🌟 핵심: 부모(Performance)를 자식(Concert/Sports)으로 형변환해서 데이터 꺼내기
        if (entity instanceof Concert) {
            Concert c = (Concert) entity;
            this.type = "CONCERT";
            this.description = c.getArtist() + " (" + c.getGenre() + ")";
        } 
        else if (entity instanceof Sports) {
            Sports s = (Sports) entity;
            this.type = "SPORTS";
            this.description = s.getHomeTeam() + " vs " + s.getAwayTeam();
        } 
        else {
            this.type = "UNKNOWN";
        }
    }
}