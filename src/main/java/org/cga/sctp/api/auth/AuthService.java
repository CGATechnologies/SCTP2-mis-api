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

import org.cga.sctp.api.config.AuthConfiguration;
import org.cga.sctp.api.core.AppConstants;
import org.cga.sctp.api.core.BaseService;
import org.cga.sctp.api.security.access_control.UserRole;
import org.cga.sctp.api.user.ApiUser;
import org.cga.sctp.api.utils.CryptoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService extends BaseService {

    @Autowired
    private AuthConfiguration configuration;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean authenticateUser(ApiUser apiUser, String password) {
        return passwordEncoder.matches(password, apiUser.getPassword());
    }

    public String generateRandomString(int count) {
        return CryptoUtils.genRandomString(count);
    }

    public String generatePassword() {
        return passwordEncoder.encode(generateRandomString(15));
    }

    public int getMaxAuthAttempts() {
        return configuration.getMaxAttempts();
    }

    public boolean canSystemAdminUseAPI() {
        return configuration.getAdmin().canAdminUseApi();
    }

    public boolean isSystemAdministratorRole(UserRole role) {
        return AppConstants.SYSTEM_ADMIN_ROLE.equals(role.getName());
    }
}
