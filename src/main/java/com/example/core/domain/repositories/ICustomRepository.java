package com.example.core.domain.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICustomRepository<T, ID> {

    Page<T> findAll(Pageable pageable);
}
