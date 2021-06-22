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

package org.cga.sctp.api.location;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.cga.sctp.api.core.IncludeGeneralResponses;
import org.cga.sctp.api.core.RequiresPermission;
import org.cga.sctp.api.core.SecuredController;
import org.cga.sctp.api.core.pagination.ItemPage;
import org.cga.sctp.api.security.access_control.UserPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDateTime;

@RestController
@Tag(name = "Location Management", description = "Endpoint for geo-location related operations.")
@RequestMapping("/locations")
public class LocationController extends SecuredController {

    @Autowired
    private LocationService locationService;

    @GetMapping
    @Operation(description = "Return a list of all locations.")
    @IncludeGeneralResponses
    @RequiresPermission
    @Secured(UserPermissions.READ_LOCATIONS)
    public ItemPage<Location> getLocationList(Pageable pageable) {
        return ItemPage.of(locationService.getLocations(pageable));
    }

    @GetMapping("/active")
    @Operation(description = "Return a list of active locations")
    @IncludeGeneralResponses
    @RequiresPermission
    @Secured(UserPermissions.READ_LOCATIONS)
    public ItemPage<Location> getActiveLocations(Pageable pageable) {
        return ItemPage.of(getLocations(pageable, true));
    }

    @GetMapping("/inactive")
    @Operation(description = "Return a list of inactive locations")
    @IncludeGeneralResponses
    @RequiresPermission
    @Secured(UserPermissions.READ_LOCATIONS)
    public ItemPage<Location> getInactiveLocations(Pageable pageable) {
        return ItemPage.of(getLocations(pageable, false));
    }

    private Page<Location> getLocations(Pageable pageable, boolean active) {
        return locationService.getLocationsByActiveStatus(pageable, active);
    }

    @PostMapping
    @Operation(description = "Add a location")
    @ApiResponses({
            @ApiResponse(responseCode = "404", description = "Parent location does not exist.", content = @Content),
            @ApiResponse(responseCode = "406", description = "Parent location type must have a higher hierarchy level.", content = @Content),
            @ApiResponse(responseCode = "412", description = "Cannot add location of this type without a parent.", content = @Content)
    })
    @IncludeGeneralResponses
    public ResponseEntity<Location> addLocation(@Valid @RequestBody AddLocationRequest request, Authentication authentication) {
        LocalDateTime now;
        Location parent;
        Location location;

        if (request.getParent() != null) {
            if ((parent = locationService.findById(request.getParent())) == null) {
                return ResponseEntity.notFound().build();
            }
            // verify hierarchy
            if (!locationService.isValidLocationHieArchyLevel(parent.getLocationType(), request.getType())) {
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
            }
        } else {
            if (!request.getType().isRoot) {
                return ResponseEntity.status(HttpServletResponse.SC_PRECONDITION_FAILED).build();
            }
        }

        now = LocalDateTime.now();
        location = new Location();
        location.setActive(true);
        location.setCreatedAt(now);
        location.setName(request.getName());
        location.setParentId(request.getParent());
        location.setLocationType(request.getType());

        locationService.addLocation(location);

        return ResponseEntity.ok(location);
    }
}

