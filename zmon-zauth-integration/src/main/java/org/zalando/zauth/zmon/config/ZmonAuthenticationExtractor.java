package org.zalando.zauth.zmon.config;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;
import org.zalando.stups.oauth2.spring.server.DefaultAuthenticationExtractor;
import org.zalando.zmon.security.AuthorityService;
import org.zalando.zmon.security.authority.ZMonUserAuthority;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * 
 * @author jbellmann
 *
 */
public class ZmonAuthenticationExtractor extends DefaultAuthenticationExtractor {

    private static final String UID = "uid";
    private static final String REALM = "realm";
    private final AuthorityService userService;

    public ZmonAuthenticationExtractor(AuthorityService userService) {
        this.userService = userService;
    }

    @Override
    protected List<GrantedAuthority> createAuthorityList(Map<String, Object> tokenInfoResponse) {
        Assert.notNull(tokenInfoResponse, "'tokenInfoResponse' should never be null");
        String realm = (String) tokenInfoResponse.getOrDefault(REALM, "unknown");
        String uid = (String) tokenInfoResponse.get(UID);
        Assert.hasText(uid, "'uid' should never be null or empty.");

        if ("/employees".equals(realm)) {
            return (List<GrantedAuthority>) userService.getAuthorities(uid);
        }

        return Lists.newArrayList(new ZMonUserAuthority(uid, ImmutableSet.of()));
    }

}
