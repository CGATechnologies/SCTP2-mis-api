/*
 * Copyright (C) 2021 CGA Technologies, a trading name of Charlie Goldsmith Associates Ltd
 *  All rights reserved, released under the BSD-3 licence.
 *
 * CGA Technologies develop and use this software as part of its work
 *  but the software itself is open-source software; you can redistribute it and/or modify
 *  it under the terms of the BSD licence below
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *  3. Neither the name of the copyright holder nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS
 *  BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 *  GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 *  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *  LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 *  OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 *  DAMAGE.
 *
 * For more information please see http://opensource.org/licenses/BSD-3-Clause
 */

package org.cga.sctp.api.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.cga.sctp.api.core.BaseController;
import org.cga.sctp.api.core.IncludeGeneralResponses;
import org.cga.sctp.api.security.JwtInfo;
import org.cga.sctp.api.security.JwtUtil;
import org.cga.sctp.api.user.ApiUser;
import org.cga.sctp.api.user.ApiUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;

@RestController
@RequestMapping
@Tag(name = "Authentication", description = "Authentication endpoint")
public class AuthController extends BaseController {

    @Autowired
    private ApiUserService apiUserService;

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/authenticate")
    @Operation(description = "Authenticates the user using username and password.")
    @ApiResponses({
            @ApiResponse(responseCode = "401", description = "Authentication failed. Invalid username or password.", content = @Content),
            @ApiResponse(responseCode = "403", description = "Authentication failed. Inactive account.", content = @Content)
    })
    @IncludeGeneralResponses
    public ResponseEntity<AuthenticationResponse> authenticateApiUser(@Valid @RequestBody AuthenticationRequest request, HttpServletRequest httpRequest) {
        final ApiUser apiUser;
        final JwtInfo jwtInfo;

        if ((apiUser = apiUserService.findByUserName(request.getUserName())) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        apiUser.setLastAuthAttemptAt(LocalDateTime.now());
        apiUser.setIpAddress(httpRequest.getRemoteAddr());

        if (authService.isSystemAdministratorRole(apiUser.getRole())) {
            if (!authService.canSystemAdminUseAPI()) {
                publishEvent(AuthenticationEvent.ofFailure(apiUser, "Administrator disabled."));
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }

        if (apiUser.isDeleted() || !apiUser.isActive() || !apiUser.getRole().isActive()) {
            publishEvent(AuthenticationEvent.ofFailure(apiUser, "Inactive principal"));
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (!authService.authenticateUser(apiUser, request.getPassword())) {
            apiUser.setAuthAttempts(apiUser.getAuthAttempts() + 1);
            apiUser.setActive(apiUser.getAuthAttempts() >= authService.getMaxAuthAttempts());

            if (!apiUser.isActive()) {
                publishEvent(AuthenticationEvent.ofFailure(apiUser, "Max allowed authentication attempts exhausted."));
                apiUser.setStatusText(format("Locked after %,d failed authentication attempts.",
                        authService.getMaxAuthAttempts()));
            }

            apiUserService.saveUser(apiUser);
            publishEvent(AuthenticationEvent.ofSuccess(apiUser));
            return ResponseEntity.status(!apiUser.isActive() ? HttpStatus.FORBIDDEN : HttpStatus.UNAUTHORIZED).build();
        }

        // Generate token
        jwtInfo = jwtUtil.generateJwt(apiUser);

        apiUser.setAuthAttempts(0);
        apiUser.setSessionId(jwtInfo.getJti());

        apiUserService.saveUser(apiUser);

        return ResponseEntity.ok(new AuthenticationResponse(jwtInfo.getToken()));
    }
}