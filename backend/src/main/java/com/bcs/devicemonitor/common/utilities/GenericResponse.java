package com.bcs.devicemonitor.common.utilities;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Standard envelope returned by every controller endpoint.
 * <p>
 * {@code returnCode} mirrors the HTTP status code, {@code returnMessage} is a
 * human-readable summary and {@code returnObject} carries the payload (a single
 * object, a list, etc.). Null fields are omitted from the serialised response.
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenericResponse implements Serializable {
    private Integer returnCode;
    private String returnMessage;
    private Object returnObject;

    public GenericResponse() {
    }

    public GenericResponse(Integer returnCode, String returnMessage) {
        this.returnCode = returnCode;
        this.returnMessage = returnMessage;
    }

    public GenericResponse(Integer returnCode, String returnMessage, Object returnObject) {
        this.returnCode = returnCode;
        this.returnMessage = returnMessage;
        this.returnObject = returnObject;
    }
}
