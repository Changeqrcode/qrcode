package com.change.qrcode.repository;

import com.change.qrcode.model.UploadImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UploadImageRepository extends JpaRepository<UploadImage,Long> {

}
