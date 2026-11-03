package net.wowdev.ai.expensesagent.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.YearMonth;
import net.wowdev.ai.expensesagent.model.ConfigurationEntry;
import net.wowdev.ai.expensesagent.model.Expense;
import net.wowdev.ai.expensesagent.model.ExpenseType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class ExpenseRepositoryTest {
  @Autowired ExpenseRepository repository;
  @Autowired ConfigurationRepository configurationRepository;

  @Test void supportsCategoryAndSpendingQueries() {
    YearMonth currentMonth = YearMonth.now();
    LocalDate start = currentMonth.atDay(1);
    LocalDate end = currentMonth.plusMonths(1).atDay(1);
    repository.save(new Expense(null, start, "Food", "Lunch", 12.5));
    repository.save(new Expense(null, start.plusDays(1), "Food", "Dinner", 7.5));
    repository.save(new Expense(null, start.plusDays(2), "Rent", "Home", 80.0));
    repository.save(new Expense(null, start.minusDays(1), "Food", "Old lunch", 900.0));
    assertEquals(2, repository.findByCategoryIgnoreCaseAndDateGreaterThanEqualAndDateLessThan(
        "food", start, end).size());
    assertEquals(100.0, repository.getTotalSpending(start, end));
    repository.save(new Expense(null, start.plusDays(3), "Sales", "Sold old bike", 35.0, ExpenseType.CREDIT));
    repository.save(new Expense(null, start.minusDays(1), "Sales", "Old sale", 500.0, ExpenseType.CREDIT));
    assertEquals(100.0, repository.getTotalSpending(start, end));
    assertEquals(35.0, repository.getTotalCredits(start, end));
    assertEquals(2, repository.getSpendingPerCategory(start, end).size());

    ConfigurationEntry salary = configurationRepository.save(
        new ConfigurationEntry("deafult.salary", "Salary", 5000.0));
    assertEquals("Salary", configurationRepository.findById("deafult.salary").orElseThrow().getName());
    assertEquals(5000.0, salary.getValue());
  }
}
