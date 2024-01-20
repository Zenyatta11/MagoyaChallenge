package com.zenyatta.magoya.challenge.projection;

import com.zenyatta.magoya.challenge.event.util.EventEnvelope;
import com.zenyatta.magoya.challenge.event.util.VersionedView;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.CrudRepository;

@Slf4j
public abstract class JPAProjection<V, I> {
    private final CrudRepository<V, I> repository;

    protected JPAProjection(final CrudRepository<V, I> repository) {
        this.repository = repository;
    }

    protected <EventT> void add(final EventEnvelope<EventT> eventEnvelope, final Supplier<V> handle) {
        final V result = handle.get();

        if (result instanceof final VersionedView versionedView) {
            versionedView.setMetadata(eventEnvelope.metadata());
        }

        repository.save(result);
    }

    protected <EventT> void getAndUpdate(
            final I viewId,
            final EventEnvelope<EventT> eventEnvelope,
            final Function<V, V> handle) {
        final Optional<V> view = repository.findById(viewId);

        if (view.isEmpty()) {
            if (log.isWarnEnabled()) {
                log.warn("View with id %s was not found for event %s".formatted(viewId,
                        eventEnvelope.metadata().eventType()));
            }
            return;
        }

        if (view.get() instanceof final VersionedView versionedView && wasAlreadyApplied(versionedView, eventEnvelope)) {
            if (log.isWarnEnabled()) {
                log.warn("View with id %s was already applied for event %s".formatted(viewId,
                        eventEnvelope.metadata().eventType()));
            }
            return;
        }

        final V result = handle.apply(view.get());

        if (result instanceof final VersionedView versionedView) {
            versionedView.setMetadata(eventEnvelope.metadata());
        }

        repository.save(result);
    }

    protected void deleteById(final I viewId) {
        repository.deleteById(viewId);
    }

    private static boolean wasAlreadyApplied(final VersionedView view, final EventEnvelope<?> eventEnvelope) {
        return view.getLastProcessedPosition() >= eventEnvelope.metadata().logPosition();
    }
}
