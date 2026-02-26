package com.example.demo.service;

import com.example.demo.model.PatientProfile;
import com.example.demo.model.User;
import com.example.demo.repository.PatientProfileRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PatientProfileBackfillRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PatientProfileRepository patientProfileRepository;

    public PatientProfileBackfillRunner(UserRepository userRepository,
                                        PatientProfileRepository patientProfileRepository) {
        this.userRepository = userRepository;
        this.patientProfileRepository = patientProfileRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        int created = 0;

        for (User user : userRepository.findAll()) {
            String role = user.getRole() != null ? user.getRole().toUpperCase() : "";
            if (!role.endsWith("PATIENT")) {
                continue;
            }

            if (patientProfileRepository.findByUser(user).isPresent()) {
                continue;
            }

            PatientProfile profile = new PatientProfile();
            profile.setUser(user);
            profile.setFullName(user.getFullName() != null ? user.getFullName() : user.getName());
            profile.setPhone(user.getPhone());
            profile.setAddress(user.getAddress());
            patientProfileRepository.save(profile);
            created++;
        }

        if (created > 0) {
            System.out.println("Startup backfill: created " + created + " missing patient profile(s).");
        }
    }
}
