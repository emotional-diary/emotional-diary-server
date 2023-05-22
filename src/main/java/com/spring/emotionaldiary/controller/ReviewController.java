package com.spring.emotionaldiary.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @GetMapping
    public ResponseEntity<String> writeReview(Authentication authentication){
        return ResponseEntity.ok(authentication.getName() + "님의 리뷰 등록 완료");
    }
}
