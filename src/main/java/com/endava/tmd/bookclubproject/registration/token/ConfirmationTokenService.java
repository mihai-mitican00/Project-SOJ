package com.endava.tmd.bookclubproject.registration.token;

import com.endava.tmd.bookclubproject.exception.ApiBadRequestException;
import com.endava.tmd.bookclubproject.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

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

    public ConfirmationToken generateConfirmationToken(final User user){
        String token = UUID.randomUUID().toString();
        return new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                user
        );
    }

    public void deleteAllTokensOfAnUser(final Long userId){
        confirmationTokenRepository.deleteAllByUserId(userId);
    }
}
