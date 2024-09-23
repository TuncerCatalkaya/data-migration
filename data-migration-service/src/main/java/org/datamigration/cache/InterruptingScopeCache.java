package org.datamigration.cache;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Getter
public class InterruptingScopeCache {

    private final Set<UUID> interruptingScopes = ConcurrentHashMap.newKeySet();

}
