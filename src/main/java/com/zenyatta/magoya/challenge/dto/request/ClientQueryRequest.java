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
