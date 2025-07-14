package app.dns.model.util.jmx.auth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.management.remote.JMXAuthenticator;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;

public class CustomAuthenticator implements JMXAuthenticator {
    private static Logger logger = LogManager.getLogger(CustomAuthenticator.class);

    @Override
    public Subject authenticate(Object credentials) {
        logger.info("Authentication initialized");
        if (!(credentials instanceof String[])) {
            throw new SecurityException("Credentials not String[]");
        }
        final String[] aCredentials = (String[]) credentials;
        if (aCredentials.length != 2) {
            throw new SecurityException("Credentials should be username/password");
        }

        try {
            LoginContext loginContext = new LoginContext("JMXConfig", new CallbackHandler() {
                @Override
                public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                    for (Callback callback : callbacks) {
                        if (callback instanceof NameCallback) {
                            ((NameCallback) callback).setName(aCredentials[0]);
                            logger.info("Setting name callback");
                        } else if (callback instanceof PasswordCallback) {
                            ((PasswordCallback) callback).setPassword((aCredentials[1].toCharArray()));
                            logger.info("Setting password callback");
                        } else {
                            throw new UnsupportedCallbackException(callback, "Unsupported Callback");
                        }
                    }
                }
            });

            loginContext.login();
            return loginContext.getSubject();
        } catch (LoginException e) {
            logger.error("Authentication failed: {}" + e.getMessage());
            throw new SecurityException("Authentication failed: " + e.getMessage(), e);
        }
    }
}
