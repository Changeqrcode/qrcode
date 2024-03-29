package com.change.qrcode.repository;

import com.change.qrcode.model.Packages;
import com.change.qrcode.model.QR;
import com.change.qrcode.model.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.UUID;

@Repository
@Transactional
public interface PackagesRepository extends JpaRepository<Packages, Long> {
    Packages findByName(String name);
}
