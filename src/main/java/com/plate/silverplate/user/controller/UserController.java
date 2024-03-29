package com.plate.silverplate.user.controller;


import com.plate.silverplate.user.domain.dto.TokenRequest;
import com.plate.silverplate.user.domain.dto.TokenResponse;
import com.plate.silverplate.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1")
@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserService userService;

    @PostMapping("reissue")
    public ResponseEntity<TokenResponse> reissue(@RequestHeader("Authorization") String requestAccessToken,
                                                 @RequestBody TokenRequest tokenRequest) {
        TokenResponse reissuedToken = userService.accessTokenByRefreshToken(requestAccessToken, tokenRequest.refreshToken());

        return ResponseEntity
                .ok(reissuedToken);
    }
}

