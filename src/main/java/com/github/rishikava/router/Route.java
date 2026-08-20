package com.github.rishikava.router;

import com.github.rishikava.http.HttpMethod;

public record Route (
    HttpMethod method,
    String path
) {
    //TODO: implement path pattern matching for endpoints   
}
