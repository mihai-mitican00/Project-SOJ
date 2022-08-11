package com.endava.tmd.bookclubproject.registration.token;

import com.endava.tmd.bookclubproject.exception.ApiBadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ConfirmationTokenService {

    @Autowired
    private ConfirmationTokenRepository confirmationTokenRepository;

    public void saveConfirmationToken(ConfirmationToken confirmationToken) {
        confirmationTokenRepository.save(confirmationToken);
    }

    public Optional<ConfirmationToken> getToken(final String token){
        return confirmationTokenRepository.findByToken(token);
    }

    public void setConfirmedAt(final String token){
       ConfirmationToken confirmationToken =
               confirmationTokenRepository
                       .findByToken(token)
                       .orElseThrow(() -> new ApiBadRequestException("Token not found!"));

       confirmationToken.setConfirmedAt(LocalDateTime.now());
    }
}
