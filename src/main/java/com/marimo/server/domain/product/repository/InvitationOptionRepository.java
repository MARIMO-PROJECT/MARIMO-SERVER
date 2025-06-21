package com.marimo.server.domain.product.repository;

import com.marimo.server.domain.product.entity.InvitationOptionEntity;
import com.marimo.server.domain.product.enums.OptionType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationOptionRepository extends JpaRepository<InvitationOptionEntity, Long> {

    List<InvitationOptionEntity> findAllByOptionTypeOrderById(final OptionType optionType);
}
