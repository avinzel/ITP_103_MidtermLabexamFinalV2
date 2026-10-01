package com.example.demo;

import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Component
public class LogStore {
    private final LinkedList<String> logs = new LinkedList<>();

    public synchronized void add(String message) {
        logs.addFirst("[" + java.time.LocalTime.now().withNano(0) + "] " + message);
        if (logs.size() > 50) logs.removeLast();
    }

    public synchronized List<String> getLogs() {
        return Collections.unmodifiableList(logs);
    }
}
