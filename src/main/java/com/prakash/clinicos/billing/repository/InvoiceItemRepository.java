package com.prakash.clinicos.billing.repository;

import com.prakash.clinicos.billing.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {

    List<InvoiceItem> findByInvoiceIdOrderById(Long invoiceId);

    void deleteByInvoiceId(Long invoiceId);
}
