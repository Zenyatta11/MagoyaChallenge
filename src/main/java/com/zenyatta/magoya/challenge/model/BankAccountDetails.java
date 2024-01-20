package com.zenyatta.magoya.challenge.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zenyatta.magoya.challenge.event.util.EventMetadata;
import com.zenyatta.magoya.challenge.event.util.VersionedView;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Builder(toBuilder = true)
@Entity
public class BankAccountDetails implements VersionedView {

    @Id
    private UUID accountId;
    
    @Column(nullable = false)
    private UUID clientId;

    @Column(nullable = false)
    private double balance;

    @JsonIgnore
    @Column(nullable = false)
    private long version;

    @JsonIgnore
    @Column(nullable = false)
    private long lastProcessedPosition;

    @JsonIgnore
    public void setMetadata(final EventMetadata eventMetadata) {
        this.version =  eventMetadata.streamPosition();
        this.lastProcessedPosition = eventMetadata.logPosition();
    }
}
