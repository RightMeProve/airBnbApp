package com.rightmeprove.airbnb.airBnbApp.controller;

import com.rightmeprove.airbnb.airBnbApp.dto.InventoryDto;
import com.rightmeprove.airbnb.airBnbApp.dto.UpdateInventoryRequestDto;
import com.rightmeprove.airbnb.airBnbApp.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/inventory") // Admin endpoints for managing hotel room inventory
@RequiredArgsConstructor // Injects InventoryService automatically
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Retrieves all inventory entries for a specific room.
     * @param roomId unique ID of the room
     * @return list of InventoryDto representing availability/pricing per date
     */
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<List<InventoryDto>> getAllInventoryByRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(inventoryService.getAllInventoryByRoom(roomId));
    }

    /**
     * Updates inventory for a specific room (e.g., availability, price changes).
     * @param roomId unique ID of the room
     * @param updateInventoryRequestDto contains updated inventory info
     * @return 204 No Content on success
     */
    @PatchMapping("/rooms/{roomId}")
    public ResponseEntity<Void> updateInventory(@PathVariable Long roomId,
                                                @RequestBody UpdateInventoryRequestDto updateInventoryRequestDto) {
        inventoryService.updateInventory(roomId, updateInventoryRequestDto);
        return ResponseEntity.noContent().build(); // standard for successful update with no body
    }
}
