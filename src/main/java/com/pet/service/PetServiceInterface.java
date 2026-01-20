package com.pet.service;

import java.util.List;
import java.util.UUID;

public interface PetServiceInterface<D> {

    /**
     * 取得所有寵物
     */
    List<D> getAll();

    /**
     * 根據 ID 取得寵物
     */
    D getById(UUID id);

    /**
     * 新增寵物
     */
    D create(D dto);

    /**
     * 更新寵物
     */
    D update(UUID id, D dto);

    /**
     * 刪除寵物
     */
    void delete(UUID id);
}
