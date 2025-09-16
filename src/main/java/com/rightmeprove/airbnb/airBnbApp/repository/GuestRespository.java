package com.rightmeprove.airbnb.airBnbApp.repository;

import com.rightmeprove.airbnb.airBnbApp.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRespository extends JpaRepository<Guest,Long> {
}
