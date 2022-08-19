package com.emailScheduler.emailScheduler.Controller;

import com.emailScheduler.emailScheduler.Model.MailModel;
import com.emailScheduler.emailScheduler.Model.UserModel;
import com.emailScheduler.emailScheduler.Repository.MailModelRepository;
import com.emailScheduler.emailScheduler.Service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MailController {

    @Autowired
    MailService mailService;

    @Autowired
    MailModelRepository mailModelRepository;

    @GetMapping("getEmail")
    public ModelAndView getEmail(UserModel userModel){
        ModelAndView mav = new ModelAndView("getEmail");
        mav.addObject("user", userModel);
        return mav;
    }

    @GetMapping("confirmation")
    public ModelAndView sendConfirmationEmail(@ModelAttribute UserModel userModel) throws MessagingException, UnsupportedEncodingException {
        ModelAndView mav = new ModelAndView("confirmationPage");
        mav.addObject("user", userModel);

        mailModelRepository.save(userModel);

        String emailSubject = "Subscribed";

        Map<String, Object> map = new HashMap<>();
        map.put("userFirstName", userModel.getUserFirstName());
        map.put("userLastName", userModel.getUserLastName());
        map.put("userEmail", userModel.getUserEmail());

        MailModel mailModel = new MailModel(userModel.getUserEmail(), emailSubject, map);

        mailService.sendHTMLMail(mailModel, "confirmationEmailTemplate");
        return mav;
    }

    @GetMapping("unsubscribe/{email}")
    public ModelAndView unsubscribe(@PathVariable String email){
        ModelAndView mav = new ModelAndView("unsubscribe");
        mailModelRepository.deleteById(email);
        return mav;
    }

    //@Scheduled(cron="0 0 10 */ * * ")
    @Scheduled(cron="0 */1 * * * * ")
    public void sendMails(){

        String emailSubject = "Message of the day";

        List<UserModel> allUsers = mailModelRepository.findAll();
        if (allUsers != null) {
            for (UserModel eachUser : allUsers) {

                Map<String, Object> map = new HashMap<>();
                map.put("userFirstName", eachUser.getUserFirstName());
                map.put("userLastName", eachUser.getUserLastName());
                map.put("userEmail", eachUser.getUserEmail());

                MailModel mailModel = new MailModel(eachUser.getUserEmail(), emailSubject, map);
                try {
                    mailService.sendHTMLMail(mailModel, "scheduledEmailTemplate");
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
