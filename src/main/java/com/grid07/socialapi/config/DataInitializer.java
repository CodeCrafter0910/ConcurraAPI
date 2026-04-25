package com.grid07.socialapi.config;

import com.grid07.socialapi.entity.Bot;
import com.grid07.socialapi.entity.User;
import com.grid07.socialapi.repository.BotRepository;
import com.grid07.socialapi.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BotRepository botRepository;

    public DataInitializer(UserRepository userRepository, BotRepository botRepository) {
        this.userRepository = userRepository;
        this.botRepository = botRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User u1 = new User();
            u1.setUsername("alice");
            u1.setPremium(true);
            userRepository.save(u1);

            User u2 = new User();
            u2.setUsername("bob");
            u2.setPremium(false);
            userRepository.save(u2);

            User u3 = new User();
            u3.setUsername("charlie");
            u3.setPremium(false);
            userRepository.save(u3);
        }

        if (botRepository.count() == 0) {
            Bot b1 = new Bot();
            b1.setName("TrendBot");
            b1.setPersonaDescription("Engages with trending posts and adds quick reactions.");
            botRepository.save(b1);

            Bot b2 = new Bot();
            b2.setName("SummaryBot");
            b2.setPersonaDescription("Summarizes long posts into bite-sized responses.");
            botRepository.save(b2);

            Bot b3 = new Bot();
            b3.setName("HypeBot");
            b3.setPersonaDescription("Amplifies viral content by boosting engagement early.");
            botRepository.save(b3);
        }
    }
}
