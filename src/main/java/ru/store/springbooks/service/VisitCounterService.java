package ru.store.springbooks.service;

import java.util.Map;

public interface VisitCounterService {


    public void incrementVisitCount(String url);

    public int getVisitCount(String url);

    public int getTotalVisitCount();

    public Map<String, Integer> getAllVisitCounts();

    }
