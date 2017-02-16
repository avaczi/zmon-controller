package org.zalando.zmon.controller;

import com.codahale.metrics.Meter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.WebUtils;
import org.zalando.zmon.config.ControllerProperties;
import org.zalando.zmon.security.tvtoken.TvTokenService;
import org.zalando.zmon.service.OneTimeTokenService;
import org.zalando.zmon.service.TokenRequestResult;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.concurrent.TimeUnit.DAYS;
import static org.zalando.zmon.security.tvtoken.TvTokenService.X_FORWARDED_FOR;

/**
 * @author jbellmann
 */
@Controller
public class TvTokenController {

    private final Logger log = LoggerFactory.getLogger(TvTokenController.class);

    private final TvTokenService tvTokenService;
    private final OneTimeTokenService oneTimeTokenService;

    private final Meter rateLimit = new Meter();

    private final ControllerProperties config;
    private AtomicLong lastRequest = new AtomicLong(0);

    @Autowired
    public TvTokenController(TvTokenService tvTokenService, OneTimeTokenService oneTimeTokenService, ControllerProperties config) {
        this.tvTokenService = tvTokenService;
        this.oneTimeTokenService = oneTimeTokenService;
        this.config = config;
    }

    @RequestMapping("/tv/by-email")
    public String getEmailForm(Model model) {
        model.addAttribute("domain", config.getEmailTokenDomain());
        model.addAttribute("staticUrl", config.getStaticUrl());
        return "by-email";
    }

    /* we allow only one request every 15 sec to get through, another global limit is in the DB */
    private boolean isRateLimitHit() {long time = System.currentTimeMillis();
        long lastTime = lastRequest.get();
        if (time - lastTime < 15000) {
            return true;
        }

        return !lastRequest.compareAndSet(lastTime, time);
    }

    @RequestMapping(path="/tv/by-email", method= RequestMethod.POST)
    public ResponseEntity<String> getByEMail(@RequestParam(value="mail") String mail,
                                             @RequestHeader(name = X_FORWARDED_FOR, required = false) String bindIp,
                                             HttpServletRequest request) {
        if(mail == null || "".equals(mail) || mail.contains("@") || mail.contains("%40")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (bindIp == null) {
            bindIp = remoteIp(request);
        }

        if(isRateLimitHit()) {
            return new ResponseEntity<>("SEND_FAILED_RATE_LIMIT", HttpStatus.TOO_MANY_REQUESTS);
        }

        try {
            TokenRequestResult sent = oneTimeTokenService.sendByEmail(mail, bindIp);
            switch(sent) {
                case OK: return new ResponseEntity<>("SENT_SUCCESSFULLY", HttpStatus.OK);
                case RATE_LIMIT_HIT: return new ResponseEntity<>("SEND_FAILED_RATE_LIMIT", HttpStatus.TOO_MANY_REQUESTS);
            }
        }
        catch(Throwable t) {
            log.error("Error during mail send: email={} ip={} msg={}", mail, bindIp, t.getMessage());
        }

        return new ResponseEntity<>("SEND_FAILED", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @RequestMapping("/tv/{token}")
    public String handleToken(@PathVariable String token,
                              @RequestHeader(name = X_FORWARDED_FOR, required = false) String bindIp,
                              HttpServletRequest request,
                              HttpServletResponse response) {
        if (StringUtils.hasText(token) && token.length() > 5) {
            // check-token
            if (bindIp == null) {
                bindIp = remoteIp(request);
            }
            String bindRandom = UUID.randomUUID().toString();
            rateLimit.mark();
            if (rateLimit.getOneMinuteRate() < 5 && isValidToken(token, bindIp, bindRandom)) {
                bindZmonTvCookie(token, request, response);
                bindZmonUidCookie(bindRandom, request, response);
            } else {
                tvTokenService.deleteCookiesIfExistent(request, response);
            }
        } else {
            tvTokenService.deleteCookiesIfExistent(request, response);
        }
        return "redirect:/";
    }

    protected boolean isValidToken(String token, String bindIp, String sessionId) {
        return tvTokenService.isValidToken(token, bindIp, sessionId);
    }

    public static String remoteIp(HttpServletRequest request) {
        if (request != null) {
            return request.getRemoteAddr();
        }
        return "UNKNOWN";
    }

    protected void bindZmonTvCookie(String token, HttpServletRequest request, HttpServletResponse response) {
        String cookieValue = Base64Utils.encodeToString(token.getBytes());
        Cookie cookie = WebUtils.getCookie(request, TvTokenService.ZMON_TV);
        if (cookie == null) {
            cookie = new Cookie(TvTokenService.ZMON_TV, cookieValue);
        } else {
            cookie.setValue(cookieValue);
        }
        cookie.setComment("ZMON_TV enables access for Team monitors.");
        cookie.setMaxAge((int) DAYS.toSeconds(365));
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
    }

    private void bindZmonUidCookie(String bindRandom, HttpServletRequest request, HttpServletResponse response) {
        String cookieValue = Base64Utils.encodeToString(bindRandom.getBytes());
        Cookie cookie = WebUtils.getCookie(request, TvTokenService.ZMON_TV_ID);
        if (cookie == null) {
            cookie = new Cookie(TvTokenService.ZMON_TV_ID, cookieValue);
        } else {
            cookie.setValue(cookieValue);
        }
        cookie.setComment("ZMON_TV_ID enables access for Team monitors.");
        cookie.setMaxAge((int) DAYS.toSeconds(365));
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
    }
}
