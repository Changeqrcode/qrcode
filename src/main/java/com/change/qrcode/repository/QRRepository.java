package com.change.qrcode.repository;

import com.change.qrcode.model.QR;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.UUID;

@Repository
@Transactional
public interface QRRepository extends JpaRepository<QR, UUID> {
}
