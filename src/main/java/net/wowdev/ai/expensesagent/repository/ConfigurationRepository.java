package net.wowdev.ai.expensesagent.repository;

import net.wowdev.ai.expensesagent.model.ConfigurationEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigurationRepository extends JpaRepository<ConfigurationEntry, String> {}
