package org.cga.sctp.api.core;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ApiErrors {
    private Map<String, List<String>> errors;

    public static ApiErrors addFieldError(ApiErrors apiErrors, String field, String error) {
        if (apiErrors == null) {
            apiErrors = new ApiErrors();
            apiErrors.errors = new LinkedHashMap<>();
        }
        apiErrors.errors.computeIfAbsent(field, s -> new LinkedList<>()).add(error);
        return apiErrors;
    }

    public Map<String, List<String>> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, List<String>> errors) {
        this.errors = errors;
    }
}