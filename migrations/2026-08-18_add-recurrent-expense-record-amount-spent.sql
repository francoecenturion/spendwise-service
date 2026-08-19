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
-- Run this once against the prod database (spring.jpa.hibernate.ddl-auto=none
-- there, so it will NOT be applied automatically like it is in dev).

ALTER TABLE RECURRENT_EXPENSE_RECORD
    ADD COLUMN IF NOT EXISTS AMOUNT_SPENT_IN_PESOS NUMERIC(19, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS AMOUNT_SPENT_IN_DOLLARS NUMERIC(19, 2) NOT NULL DEFAULT 0;
