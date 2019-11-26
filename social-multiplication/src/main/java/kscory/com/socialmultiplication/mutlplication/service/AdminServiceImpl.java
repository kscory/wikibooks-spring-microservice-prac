package kscory.com.socialmultiplication.mutlplication.service;

import kscory.com.socialmultiplication.mutlplication.repository.MultiplicationRepository;
import kscory.com.socialmultiplication.mutlplication.repository.MultiplicationResultAttemptRepository;
import kscory.com.socialmultiplication.mutlplication.repository.UserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("test")
@Service
public class AdminServiceImpl implements AdminService {

    private MultiplicationRepository multiplicationRepository;
    private MultiplicationResultAttemptRepository attemptRepository;
    private UserRepository userRepository;

    public AdminServiceImpl(final MultiplicationRepository multiplicationRepository,
                            final UserRepository userRepository,
                            final MultiplicationResultAttemptRepository attemptRepository) {
        this.multiplicationRepository = multiplicationRepository;
        this.userRepository = userRepository;
        this.attemptRepository = attemptRepository;
    }

    @Override
    public void deleteDatabaseContents() {
        attemptRepository.deleteAll();
        multiplicationRepository.deleteAll();
        userRepository.deleteAll();
    }
}