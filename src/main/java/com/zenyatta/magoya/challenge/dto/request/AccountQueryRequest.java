package com.zenyatta.magoya.challenge.dto.request;

import com.zenyatta.magoya.challenge.model.BankAccountDetails;
import com.zenyatta.magoya.challenge.repository.BankAccountDetailsRepository;
import com.zenyatta.magoya.challenge.utils.http.ETag;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.lang.Nullable;
import org.springframework.retry.support.RetryTemplate;

/**
 * This record is a request entity to query bank account details.
 * The ETag is nullable. If it happens to be null, the newest version will
 * be returned. Otherwise, a version newer than the specified version will be returned.
 * 
 * @param bankAccountId The ID of the bank account
 * @param ETag          The ETag of the entity (Nullable)
 * @param repository    The repository to search in
 */
public record AccountQueryRequest(
        UUID bankAccountId,
        @Nullable ETag eTag,
        BankAccountDetailsRepository repository) {

    /**
     * This method will search in the repository for the specified account. It
     * will retry for a total of 5 seconds should it not find an account. Although
     * an account exists, it may return <code>not found</code> in certain write operation
     * coincidences.

     * @return bankAccountDetails       The BankAccountDetails entity with the specified information
     * @throws EntityNotFoundException  
     */
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
