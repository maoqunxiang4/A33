package com.fuchuang.A33.controller;

import com.fuchuang.A33.service.Impl.IntelligentSchedulingServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/scheduling")
public class SchedulingController {
    @Autowired
    private IntelligentSchedulingServiceImpl intelligentSchedulingService ;

    @PostMapping("/model1")
    public void IntelligentSchedulingAlgorithm(){
        intelligentSchedulingService.IntelligentSchedulingAlgorithm(LocalDateTime.now());
    }
}
