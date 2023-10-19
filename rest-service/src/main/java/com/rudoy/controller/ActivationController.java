package com.rudoy.controller;

import com.rudoy.service.UserActivationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class ActivationController {
    private final UserActivationService userActivationService;

    @GetMapping("/activation")
    public ResponseEntity<?> activation(@RequestParam String id) {
        //TODO сделать обоаботку разных случаев(некорректный ввод и т.д.)
        boolean activation = userActivationService.activation(id);
        if (activation) {
            return ResponseEntity.ok().body("Registration successful!");
        }
        return ResponseEntity.internalServerError().build();
    }
}
