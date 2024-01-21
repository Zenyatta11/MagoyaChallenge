package com.zenyatta.magoya.challenge.dto.request;

import com.zenyatta.magoya.challenge.model.BankAccountDetails;
import com.zenyatta.magoya.challenge.repository.BankAccountDetailsRepository;
import com.zenyatta.magoya.challenge.utils.LoggerStrings;
import java.util.List;
import org.springframework.data.domain.PageRequest;

public record ClientQueryRequest(
        int pageNumber,
        int pageSize,
        BankAccountDetailsRepository repository) {

    /**
     * This class is in charge of bringing all of the existing bank accounts
     * and their full details in paged form.

    * @param pageNumber    The page number offset (0 means the first <code>pageSize</code> entries)
    * @param pageSize      The page size (20 means each page contains 20 entries)
    * @param repository    The repository to search in
    */
    public ClientQueryRequest {
        if (pageNumber < 0) {
            throw new IllegalArgumentException(LoggerStrings.INVALID_PAGE_NUMBER);
        }

        if (pageSize < 0) {
            throw new IllegalArgumentException(LoggerStrings.INVALID_PAGE_SIZE);
        }
    }

    public List<BankAccountDetails> handle() {
        return repository
                .findAll(
                        PageRequest.of(this.pageNumber, this.pageSize))
                .stream().toList();
    }
}
