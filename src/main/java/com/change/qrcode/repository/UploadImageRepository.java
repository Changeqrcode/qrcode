package com.change.qrcode.repository;

import com.change.qrcode.model.UploadImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;


@Repository
@Transactional
public interface UploadImageRepository extends JpaRepository<UploadImage,Long> {

    @Transactional
    List<UploadImage> findByQRId(UUID id);

    @Transactional
    void deleteAllByQRId(UUID id);
}
