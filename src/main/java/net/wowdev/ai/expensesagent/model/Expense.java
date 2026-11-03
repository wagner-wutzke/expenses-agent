package net.wowdev.ai.expensesagent.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "expenses")
public class Expense {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private LocalDate date;
  private String category;
  @Lob private String description;
  private Double amount;

  @Enumerated(EnumType.STRING)
  private ExpenseType type = ExpenseType.DEBIT;

  public Expense(Long id, LocalDate date, String category, String description, Double amount) {
    this(id, date, category, description, amount, ExpenseType.DEBIT);
  }

  @PrePersist
  void defaultType() {
    if (type == null) {
      type = ExpenseType.DEBIT;
    }
  }
}
