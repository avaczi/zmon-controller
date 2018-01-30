package org.zalando.zmon.security.tvtoken;

import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.util.WebUtils;
import org.zalando.zmon.domain.OnetimeTokenInfo;
import org.zalando.zmon.persistence.OnetimeTokensSProcService;

public class TvTokenService {

    public static final String ZMON_TV_ID = "ZMON_TV_ID";

    public static final String ZMON_TV = "ZMON_TV";

    public static final String X_FORWARDED_FOR = "X-FORWARDED-FOR";

    private final OnetimeTokensSProcService oneTimeTokenSProcService;

    public TvTokenService(OnetimeTokensSProcService onetimeTokensSProcService) {
        this.oneTimeTokenSProcService = onetimeTokensSProcService;
    }

    public static String remoteIp(HttpServletRequest request) {
        if (request != null) {
            return request.getRemoteAddr();
        }
        return "UNKNOWN";
    }

    // TODO use sproc
    public boolean isValidToken(String token, String bindIp, String sessionId) {
        List<OnetimeTokenInfo> result = oneTimeTokenSProcService.bindOnetimeToken(token, bindIp, sessionId);
        return result.size() == 1;
    }

    public void deleteCookiesIfExistent(HttpServletRequest request, HttpServletResponse response) {
        Cookie zmonTvCookie = WebUtils.getCookie(request, TvTokenService.ZMON_TV);
        if (zmonTvCookie != null) {
            zmonTvCookie.setMaxAge(0);
            response.addCookie(zmonTvCookie);
        }
        Cookie zmonIdCookie = WebUtils.getCookie(request, TvTokenService.ZMON_TV_ID);
        if (zmonIdCookie != null) {
            zmonIdCookie.setMaxAge(0);
            response.addCookie(zmonIdCookie);
        }
    }
}
