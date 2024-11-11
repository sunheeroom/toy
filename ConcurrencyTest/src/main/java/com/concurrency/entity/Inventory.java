package com.concurrency.entity;

import com.concurrency.exception.SoldOutException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    int stock;

    @Version
    Long version;

    public void decrementStock() {
        if (this.stock > 0) {
            this.stock--;
        } else {
            throw new SoldOutException("재고가 부족합니다");
        }
    }
}