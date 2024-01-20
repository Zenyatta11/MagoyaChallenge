package com.zenyatta.magoya.challenge.repository;

import com.zenyatta.magoya.challenge.model.BankAccountDetails;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BankAccountDetailsRepository extends JpaRepository<BankAccountDetails, UUID> {
    
    @Query("SELECT ad FROM BankAccountDetails ad WHERE ad.accountId = ?1 AND ad.version > ?2")
    Optional<BankAccountDetails> findByIdAndNewerVersion(UUID id, long version);

    Optional<BankAccountDetails> findById(UUID accountId);
}
