package com.zenyatta.magoya.challenge.event.util;

public interface VersionedView {

    long getLastProcessedPosition();
    
    void setMetadata(EventMetadata eventMetadata);

}
