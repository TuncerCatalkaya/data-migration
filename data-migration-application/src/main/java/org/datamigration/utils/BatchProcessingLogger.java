package org.datamigration.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;

import java.util.UUID;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BatchProcessingLogger {

    public static void log(Level level, String fileName, UUID scopeId, String msg) {
        log.atLevel(level).log(fileName + "(" + scopeId + "), " + msg);
    }

}
