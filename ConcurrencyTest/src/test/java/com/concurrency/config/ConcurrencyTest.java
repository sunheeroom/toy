package com.concurrency.config;

import com.concurrency.entity.Inventory;
import com.concurrency.repository.InventoryRepository;
import com.concurrency.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
public class ConcurrencyTest {

    @Autowired
    InventoryService inventoryService;

    @Autowired
    InventoryRepository inventoryRepository;

    Long id;

    @BeforeEach
    void setUp() {
        Inventory inventory = new Inventory();
        inventory.setStock(20);
        Inventory savedInventory = inventoryRepository.save(inventory);
        id = savedInventory.getId();
    }

    @Test
    public void test() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successThreadCount = new AtomicInteger(0);
        long start = System.currentTimeMillis();

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                Thread currentThread = Thread.currentThread();
                try {
                    inventoryService.deduct(id);
                    successThreadCount.incrementAndGet();
                    log.info("stock count after deduct: {} ## success thread count: {} ##",
                            inventoryService.findById(id).getStock(), successThreadCount);
                } catch (Exception e) {
                    log.error("##### thread {} failed: {} #####", currentThread.getName(), e.getMessage(), e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        long end = System.currentTimeMillis();
        log.info("optimistic lock 실행 시간: {}ms", (end - start));
        log.info("최종 재고 상태: {}", inventoryService.findById(id).getStock());
        log.info("성공한 스레드 수: {}", successThreadCount.get());

        // 재고가 0인지 확인하는 검증 추가
        assertEquals(0, inventoryRepository.findById(id).get().getStock(), "최종 재고 상태가 예상과 다릅니다.");
    }
}
