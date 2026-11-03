package net.wowdev.ai.expensesagent.repository;

import java.time.LocalDate;
import java.util.List;
import net.wowdev.ai.expensesagent.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

  List<Expense> findByCategoryIgnoreCaseAndDateGreaterThanEqualAndDateLessThan(
      String category, LocalDate startDate, LocalDate endDate);

  List<Expense> findByDateGreaterThanEqualAndDateLessThanOrderByDateAsc(
      LocalDate startDate, LocalDate endDate);

  @Query("SELECT COALESCE(SUM(e.amount), 0.0) FROM Expense e WHERE e.type = DEBIT AND e.date >= :startDate AND e.date < :endDate")
  Double getTotalSpending(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

  @Query("SELECT COALESCE(SUM(e.amount), 0.0) FROM Expense e WHERE e.type = net.wowdev.ai.expensesagent.model.ExpenseType.CREDIT AND e.date >= :startDate AND e.date < :endDate")
  Double getTotalCredits(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

  @Query("SELECT e.category, SUM(e.amount) FROM Expense e WHERE e.type = DEBIT AND e.date >= :startDate AND e.date < :endDate GROUP BY e.category")
  List<Object[]> getSpendingPerCategory(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
