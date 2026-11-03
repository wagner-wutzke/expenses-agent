package net.wowdev.ai.expensesagent.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "configurations")
public class ConfigurationEntry {

  @Id
  @Column(name = "`key`", nullable = false, length = 120)
  private String key;

  @Column(name = "name", nullable = false, length = 120)
  private String name;

  @Column(name = "`value`", nullable = false)
  private Double value;

  protected ConfigurationEntry() {}

  public ConfigurationEntry(String key, String name, Double value) {
    this.key = key;
    this.name = name;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public String getName() {
    return name;
  }

  public Double getValue() {
    return value;
  }

  public void setValue(Double value) {
    this.value = value;
  }
}
