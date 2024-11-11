package com.concurrency.service;

import com.concurrency.entity.Inventory;
import com.concurrency.exception.ResourceNotFoundException;
import com.concurrency.repository.InventoryRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    @Retryable(retryFor = {OptimisticLockException.class, OptimisticLockingFailureException.class},
            maxAttempts = 10, backoff = @Backoff(delay = 1000))
    @Transactional
    public void deduct(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);
        inventory.decrementStock();

    }

    public Inventory findById(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);
    }
}
