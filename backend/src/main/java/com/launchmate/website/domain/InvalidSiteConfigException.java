package com.launchmate.website.domain;

import com.launchmate.shared.exception.BusinessRuleException;

import java.util.List;

public class InvalidSiteConfigException extends BusinessRuleException {

    public InvalidSiteConfigException(List<String> problems) {
        super("The website configuration is invalid", problems);
    }
}
