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

package org.cga.sctp.api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import org.cga.sctp.api.auth.AccessTokenClaims;
import org.cga.sctp.api.config.JwtConfiguration;
import org.cga.sctp.api.core.AppConstants;
import org.cga.sctp.api.core.BaseComponent;
import org.cga.sctp.api.user.ApiUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Component
public final class JwtUtil extends BaseComponent {
    final private Gson gson;
    final private Algorithm algorithm;
    final private JWTVerifier jwtVerifier;
    final private JwtConfiguration jwtConfiguration;

    @Autowired
    public JwtUtil(JwtConfiguration jwtConfiguration, Gson gson) {
        this.gson = gson;
        this.jwtConfiguration = jwtConfiguration;
        this.algorithm = Algorithm.HMAC256(jwtConfiguration.getSecret());
        this.jwtVerifier = JWT.require(algorithm)
                .withIssuer(jwtConfiguration.getIssuer())
                .withClaimPresence(AppConstants.JWT_ACCESS_TOKEN_CLAIM)
                .build();
    }

    /**
     * Generate a signed JWT and encapsulate the given user details in the claims.
     *
     * @param apiUser Api user to add to claims.
     * @return A signed JWT
     */
    public JwtInfo generateJwt(ApiUser apiUser) {
        final Instant now;
        final String jti;

        now = Instant.now();
        jti = UUID.randomUUID().toString();
        return new JwtInfo(
                jti,
                JWT.create()
                        .withJWTId(jti)
                        .withIssuedAt(Date.from(now))
                        .withSubject(apiUser.getUserName())
                        .withIssuer(jwtConfiguration.getIssuer())
                        .withExpiresAt(Date.from(now.plus(jwtConfiguration.getExpiration(), ChronoUnit.MINUTES)))
                        .withClaim(AppConstants.JWT_ACCESS_TOKEN_CLAIM, gson.toJson(new AccessTokenClaims(apiUser)))
                        .sign(algorithm)
        );
    }

    /**
     * Parse and verify the give JWT
     *
     * @param token Signed JWT token
     * @return Claims set in {@link #generateJwt(ApiUser)} of null if the token is invalid (structurally or logically)
     */
    public DecodedJWT parseJwt(String token) {
        try {
            return this.jwtVerifier.verify(token);
        } catch (JWTVerificationException e) {
            LOG.debug("Error parsing JWT.", e);
            return null;
        }
    }

    public AccessTokenClaims getAccessTokenClaims(DecodedJWT jwt) {
        return gson.fromJson(
                jwt.getClaim(AppConstants.JWT_ACCESS_TOKEN_CLAIM).asString(),
                AccessTokenClaims.class
        );
    }
}
