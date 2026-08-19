-- Adds the real "amount spent" accumulator to RECURRENT_EXPENSE_RECORD.
--
-- Context: budget "Pagado" totals used to be computed from the shared
-- RECURRENT_EXPENSE budgeted-amount template, which a matching Expense would
-- silently overwrite (corrupting the current AND every future month, since
-- that template row is reused across months). The fix makes each month's
-- record accumulate the real spent amount instead, independent of the
-- template. See ExpenseService.autoCancelRecurrentExpense/
-- reverseRecurrentExpenseAccumulation and BudgetService.toDTO.
--
-- IF NOT EXISTS guards against re-applying this if it was already run by
-- hand from migrations/2026-08-18_add-recurrent-expense-record-amount-spent.sql
-- before Flyway was introduced.

ALTER TABLE RECURRENT_EXPENSE_RECORD
    ADD COLUMN IF NOT EXISTS AMOUNT_SPENT_IN_PESOS NUMERIC(19, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS AMOUNT_SPENT_IN_DOLLARS NUMERIC(19, 2) NOT NULL DEFAULT 0;
