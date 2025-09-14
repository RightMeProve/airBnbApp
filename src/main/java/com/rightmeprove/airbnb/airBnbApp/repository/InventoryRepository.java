package com.rightmeprove.airbnb.airBnbApp.repository;

import com.rightmeprove.airbnb.airBnbApp.entity.Inventory;
import com.rightmeprove.airbnb.airBnbApp.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory,Long> {

    void deleteByDateAfterAndRoom(LocalDate today, Room room);
}
