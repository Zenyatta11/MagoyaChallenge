package com.zenyatta.magoya.challenge.dto.request;

import com.zenyatta.magoya.challenge.model.BankAccountDetails;
import com.zenyatta.magoya.challenge.repository.BankAccountDetailsRepository;
import com.zenyatta.magoya.challenge.utils.http.ETag;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.lang.Nullable;
import org.springframework.retry.support.RetryTemplate;

public record AccountQueryRequest(
        UUID bankAccountId,
        @Nullable ETag eTag,
        BankAccountDetailsRepository repository) {

    public BankAccountDetails handle() {
        RetryTemplate retryTemplate = RetryTemplate.builder()
                .retryOn(EntityNotFoundException.class)
                .exponentialBackoff(100, 2, 1000)
                .withinMillis(5000)
                .build();

        return retryTemplate.execute(context -> {
            Optional<BankAccountDetails> result = eTag == null ? repository.findById(bankAccountId)
                    : repository.findByIdAndNewerVersion(bankAccountId, eTag.toLong());

            if (result.isEmpty()) {
                throw new EntityNotFoundException();
            }

            return result.get();
        });

    }
}
