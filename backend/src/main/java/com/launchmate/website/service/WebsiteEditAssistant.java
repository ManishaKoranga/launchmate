package com.launchmate.website.service;

import com.launchmate.business.domain.Business;
import com.launchmate.website.domain.SiteConfig;

import java.util.List;

/** Port: turns an owner's natural-language edit request into a complete updated configuration. */
public interface WebsiteEditAssistant {

    SiteConfig propose(Business business, SiteConfig current, String instruction, List<String> correction,
                       SiteConfig previousAttempt);
}
