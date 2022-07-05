package com.kuney.community.application.service;

import java.time.LocalDate;

/**
 * @author kuneychen
 * @since 2022/7/5 21:10
 */
public interface DataService {
    long getUV(LocalDate begin, LocalDate end);

    long getDAU(LocalDate begin, LocalDate end);

    void recordUV(String ip);

    void recordDAU(int userId);
}
