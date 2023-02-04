package com.change.qrcode.repository;

import com.change.qrcode.model.ErrorMessage;
import com.change.qrcode.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErrorMessageRepository extends JpaRepository<ErrorMessage, Long> {
}
