package edu.harvard.iq.dataverse.confirmemail;

import edu.harvard.iq.dataverse.DataverseServiceBean;
import edu.harvard.iq.dataverse.DataverseSession;
import edu.harvard.iq.dataverse.ValidateEmail;
import edu.harvard.iq.dataverse.actionlogging.ActionLogRecord;
import edu.harvard.iq.dataverse.actionlogging.ActionLogServiceBean;
import edu.harvard.iq.dataverse.authorization.AuthenticationServiceBean;
import edu.harvard.iq.dataverse.authorization.AuthenticationProvider;
import edu.harvard.iq.dataverse.authorization.providers.builtin.BuiltinAuthenticationProvider;
import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.hibernate.validator.constraints.NotBlank;

/**
 * @todo: Figure out what's going on with confirmEmail method
 *        Determine whether actionlog recording will stay or not
 *        
 * @author bsilverstein
 */

@ViewScoped
@Named("ConfirmEmailPage")
public class ConfirmEmailPage implements java.io.Serializable {

    private static final Logger logger = Logger.getLogger(ConfirmEmailPage.class.getCanonicalName());

    @EJB
    ConfirmEmailServiceBean confirmEmailService;
    @EJB //maybe make a shib and builtin user service then divide later?
    AuthenticationServiceBean dataverseUserService;
    @EJB
    DataverseServiceBean dataverseService;    
    @EJB
    AuthenticationServiceBean authSvc;
    @Inject
    DataverseSession session;
    
    @EJB
    ActionLogServiceBean actionLogSvc;
    
    /**
     * The unique string used to look up a user and continue the email confirmation.
     */
    String token;

    /**
     * The user looked up by the token who will be confirming their email.
     */
    AuthenticatedUser user;

    /**
     * The email address that is entered to be confirmed.
     */
    @NotBlank(message = "Please enter a valid email address.")
    @ValidateEmail(message = "Confirm email page default email message.")    
    String emailAddress;

    /**
     * The link that is emailed to the user to confirm the email that contains
     * a token.
     */
    String confirmEmailUrl;

    
    
    ConfirmEmailData confirmEmailData;

    public void init() {
        if (token != null) {
            ConfirmEmailExecResponse confirmEmailExecResponse = confirmEmailService.processToken(token);
            confirmEmailData = confirmEmailExecResponse.getConfirmEmailData();
            if (confirmEmailData != null) {
                user = confirmEmailData.getAuthenticatedUser();
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Confirm Email Link", "Your email confirmation link is not valid."));
            }
        }
    }

    public String sendEmailConfirmLink() {
            //Need to figure out a replacement for action type _____user, was builtin before
        actionLogSvc.log( new ActionLogRecord(ActionLogRecord.ActionType.BuiltinUser, "confirmEmailRequest")
                            .setInfo("Email Address: " + emailAddress) );
        try {
            ConfirmEmailInitResponse confirmEmailInitResponse = confirmEmailService.beginConfirm(emailAddress);
            ConfirmEmailData confirmEmailData = confirmEmailInitResponse.getConfirmEmailData();
            if (confirmEmailData != null) {
                AuthenticatedUser foundUser = confirmEmailData.getAuthenticatedUser();
                confirmEmailUrl = confirmEmailInitResponse.getConfirmUrl();
                actionLogSvc.log( new ActionLogRecord(ActionLogRecord.ActionType.BuiltinUser, "passwordResetSent")
                            .setInfo("Email Address: " + emailAddress) );
            } else {                
                logger.log(Level.INFO, "Couldn''t find single account using {0}", emailAddress);
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Email Confirmation Initiated", ""));
        } catch (ConfirmEmailException ex) {            
            logger.log(Level.WARNING, "Error While confirming email: " + ex.getMessage(), ex);
        }
        return "";
    }
    //Huge mess below! Need to figure out what's up with this
//    public String confirmEmail() throws ConfirmEmailException {
//        ConfirmEmailInitResponse response;
//        response = confirmEmailService.sendConfirm(user, false);
//        try { if (response.isEmailFound()) {
//            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, response.getMessageSummary(), response.getMessageDetail()));
//            String authProviderId = AuthenticationProvider.PROVIDER_ID;
//            AuthenticatedUser au = authSvc.lookupUser(builtinAuthProviderId, user.getUserIdentifier());
//            session.setUser(au);
//            return "/dataverse.xhtml?alias=" + dataverseService.findRootDataverse().getAlias() + "faces-redirect=true";
//        } else {
//            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, response.getMessageSummary(), response.getMessageDetail()));
//            return null;
//        }
//    
//        } catch(Exception ex) { 
//            String msg = "Unable to save token for " + user.getEmail();
//            throw new ConfirmEmailException(msg, ex);
//        }
//    }
    
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public AuthenticatedUser getUser() {
        return user;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getConfirmEmailUrl() {
        return confirmEmailUrl;
    }

    public ConfirmEmailData getConfirmEmailData() {
        return confirmEmailData;
    }

    public void setConfirmEmailData(ConfirmEmailData confirmEmailData) {
        this.confirmEmailData = confirmEmailData;
    }
}    