package com.example.ajouevent_be_v2.dto.auth;

import java.util.List;

public record GooglePeopleResult(List<Organization> organizations) {

    public record Organization(String department) {
    }
}
