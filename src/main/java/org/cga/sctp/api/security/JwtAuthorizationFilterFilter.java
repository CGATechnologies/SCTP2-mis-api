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

import com.auth0.jwt.interfaces.DecodedJWT;
import org.cga.sctp.api.auth.AccessTokenClaims;
import org.cga.sctp.api.core.AppConstants;
import org.cga.sctp.api.security.access_control.UserPermission;
import org.cga.sctp.api.user.ApiUser;
import org.cga.sctp.api.user.ApiUserService;
import org.cga.sctp.api.utils.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This filter checks for a JTW token in every authenticated request.
 */
@Component
public class JwtAuthorizationFilterFilter extends GenericFilterBean {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ApiUserService userService;

    @Autowired
    private SecurityService securityService;

    private final Logger logger;

    public JwtAuthorizationFilterFilter() {
        logger = LoggerFactory.getLogger(getClass());
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        String jwt;
        final HttpServletRequest request;
        final Authentication authentication;

        request = ((HttpServletRequest) servletRequest);

        if (getRequestUri(request).equalsIgnoreCase(AppConstants.AUTHENTICATION_PATH)) {
            SecurityContextHolder.clearContext();
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (!LocaleUtils.isStringNullOrEmpty(jwt = request.getHeader(AppConstants.JWT_HEADER_NAME))) {
            if (jwt.startsWith("Bearer")) {
                jwt = jwt.substring(7);
            }

            authentication = getAuthenticationDetailsFromToken(jwt);
            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("Authenticated principal: {}", authentication.getPrincipal());
                chain.doFilter(request, servletResponse);
                return;
            }
        }

        SecurityContextHolder.clearContext();
        chain.doFilter(servletRequest, servletResponse);
    }

    private Authentication getAuthenticationDetailsFromToken(String token) {
        DecodedJWT jwt;
        ApiUser apiUser;
        AccessTokenClaims claims;

        if ((jwt = jwtUtil.parseJwt(token)) == null) {
            return null;
        }

        claims = jwtUtil.getAccessTokenClaims(jwt);
        if ((apiUser = userService.findByUserNameAndSessionId(claims.getUserName(), jwt.getId())) == null) {
            logger.warn("Invalid username and session pair for {}: Most likely a revoked session.", claims.getUserName());
            return null;
        }

        if (!apiUser.isActive() || apiUser.isDeleted() || !apiUser.getRole().isActive()) {
            return null;
        }

        // manually load permissions
        apiUser.setAuthorities(
                securityService.getRolePermissions(apiUser.getRole())
                        .stream()
                        .map((Function<UserPermission, GrantedAuthority>) userPermission -> new SimpleGrantedAuthority(userPermission.getName()))
                        .collect(Collectors.toList())
        );

        UsernamePasswordAuthenticationToken upat
                = new UsernamePasswordAuthenticationToken(apiUser.getUserName(), null, apiUser.getAuthorities());
        upat.setDetails(apiUser);

        return upat;
    }

    private String getRequestUri(HttpServletRequest request) {
        final String contextPath = request.getContextPath(), requestUri = request.getRequestURI();
        if (LocaleUtils.isStringNullOrEmpty(contextPath) || contextPath.equalsIgnoreCase("/")) {
            return requestUri;
        }
        return requestUri.substring(contextPath.length());
    }
}
