package com.example.core.domain.repositories;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ICustomRepository<T, ID> {

    List<T> findAll(Pageable pageable);
}
