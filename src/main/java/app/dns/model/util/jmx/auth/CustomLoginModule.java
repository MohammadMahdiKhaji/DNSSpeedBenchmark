package app.dns.model.util.jmx.auth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.management.remote.JMXPrincipal;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;

public class CustomLoginModule implements LoginModule {
    private static Logger logger = LogManager.getLogger(CustomLoginModule.class);

    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map<String, ?> sharedState;
    private Map<String, ?> options;

    private boolean loginSucceeded = false;
    private String username;
    private char[] password;
    private Principal userPrincipal;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
    }

    @Override
    public boolean login() throws LoginException {
        NameCallback nameCallBack = new NameCallback("username: ");
        PasswordCallback passwordCallback = new PasswordCallback("password: ", false);
        try {
            //This is calling handle from line 30 in CustomAuthenticator
            callbackHandler.handle(new Callback[]{nameCallBack, passwordCallback});
            String username = nameCallBack.getName();
            String password = new String(passwordCallback.getPassword());
            if (USERNAME.equals(username) && PASSWORD.equals(password)) {
                logger.info("User: {}, logged into the jmx server", username);
                this.username = nameCallBack.getName();
                this.password = passwordCallback.getPassword();
                loginSucceeded = true;
            }
        } catch (IOException | UnsupportedCallbackException e) {
            logger.error("An exception occurred while trying to retrieve callbacks: {}", e.getMessage());
        }
        return loginSucceeded;
    }

    @Override
    public boolean commit() throws LoginException {
        if (!loginSucceeded) {
            return false;
        }
        userPrincipal = new JMXPrincipal(username);
        subject.getPrincipals().add(userPrincipal);
        logger.info("User: {}, was added to the principal", username);
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        loginSucceeded = false;
        username = null;
        password = null;
        logger.info("LoginContext authentication failed, CustomLoginModule aborted");
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        subject.getPrincipals().remove(userPrincipal);
        logger.info("User: {}, logged out", username);
        loginSucceeded = false;
        username = null;
        password = null;
        return true;
    }
}
