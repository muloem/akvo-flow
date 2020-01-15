/*
 * Copyright (C) 2019 Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo FLOW.
 *
 * Akvo FLOW is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo FLOW is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.flow.rest.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.google.appengine.api.utils.SystemProperty;

public class EntryPoint implements AuthenticationEntryPoint {
    private final AuthenticationEntryPoint devEntryPoint;
    private final AuthenticationEntryPoint prodEntryPoint;


    public EntryPoint(AuthenticationEntryPoint prodEntryPoint, AuthenticationEntryPoint devEntryPoint) {
        this.devEntryPoint = devEntryPoint;
        this.prodEntryPoint = prodEntryPoint;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
            prodEntryPoint.commence(request, response, authException);
        } else {
            devEntryPoint.commence(request, response, authException);
        }
    }
}
