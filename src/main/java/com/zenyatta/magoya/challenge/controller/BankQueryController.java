package com.zenyatta.magoya.challenge.controller;

import com.zenyatta.magoya.challenge.dto.request.AccountQueryRequest;
import com.zenyatta.magoya.challenge.dto.request.ClientQueryRequest;
import com.zenyatta.magoya.challenge.model.BankAccountDetails;
import com.zenyatta.magoya.challenge.repository.BankAccountDetailsRepository;
import com.zenyatta.magoya.challenge.utils.http.ETag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class is the main controller for all query operations. It is
 * in charge of input validation and delegation to query processing entities.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("api/accounts")
public class BankQueryController {
    private final BankAccountDetailsRepository bankRepository;

    /**
     * This endpoint returns a bank account searched by ID. The ETag is optional.
     * Should an ETag not be provided, it will return the latest version of the entity.
     * If it happens to be provided, it will return an entity version greater than the
     * specified.

     * @param id            The bank account ID (Not client ID)
     * @param ifNoneMatch   The ETag value
     * @return              A JSON with the specified information
     */
    @GetMapping("{id}")
    ResponseEntity<BankAccountDetails> getById(
            @PathVariable final UUID id,
            @RequestHeader(name = HttpHeaders.IF_NONE_MATCH) @Parameter(in = ParameterIn.HEADER, schema = @Schema(type = "string")) @Nullable final ETag ifNoneMatch) {
        
        BankAccountDetails result;
        do {
            result = new AccountQueryRequest(id, ifNoneMatch, bankRepository).handle();
        } while(result == null);

        return ResponseEntity
                .ok()
                .eTag(ETag.weak(result.getVersion()).value())
                .body(result);
    }

    /**
     * This endpoint returns a bank account balance searched by ID. The ETag is optional.
     * Should an ETag not be provided, it will return the latest version of the entity.
     * If it happens to be provided, it will return an entity version greater than the
     * specified.

     * @param id            The bank account ID (Not client ID)
     * @param ifNoneMatch   The ETag value
     * @return              The balance value as a double.
     */
    @GetMapping("{id}/balance")
    ResponseEntity<Double> getBalanceById(
            @PathVariable final UUID id,
            @RequestHeader(name = HttpHeaders.IF_NONE_MATCH) @Parameter(in = ParameterIn.HEADER, schema = @Schema(type = "string")) @Nullable final ETag ifNoneMatch) {
        
        final BankAccountDetails result = new AccountQueryRequest(id, ifNoneMatch, bankRepository).handle();

        return ResponseEntity
                .ok()
                .eTag(ETag.weak(result.getVersion()).value())
                .body(result.getBalance());
    }

    /**
     * This endpoint returns all bank accounts stored. The page parameters are optional. Should
     * they not be provided, the default search will return the first 20 bank accounts, ordered by
     * opened date.

     * @param pageNumber    The page number offset. 0 means the first <code>pageSize</code> entries.
     * @param pageSize      The page size. This defines how many entries are returned per page.
     * @return              A JSON with the solicited information
     */
    @GetMapping
    List<BankAccountDetails> get(
            @RequestParam(defaultValue = "0") @Min(0) final Integer pageNumber,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) final Integer pageSize) {
        return new ClientQueryRequest(pageNumber, pageSize, bankRepository)
                .handle()
                .stream()
                .toList();
    }
}
