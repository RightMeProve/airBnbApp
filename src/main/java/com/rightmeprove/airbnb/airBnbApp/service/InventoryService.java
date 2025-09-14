package com.rightmeprove.airbnb.airBnbApp.service;

import com.rightmeprove.airbnb.airBnbApp.entity.Room;

public interface InventoryService {

    void initializeRoomForAYear(Room room);

    void deleteAllInventories(Room room);
}
