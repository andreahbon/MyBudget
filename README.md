# MyBudget
Budget app
Functioning app, with transactions and categories tables
- Accounts table added (17/2)
- Filter (categories and accounts) created (17/2)
- "Export to CSV" function working (sends Transactions, Categories and Accounts csv files via email)
- "Import to CSV" function working + date filter started (20/02)
- Date filter: "All transactions", "Month" and "Custom period" working. Added transaction type to Transaction Activity(22/02)
- Branch created: started working on recurring transactions table, but things were getting too complicated. Will try a different approach (26/02)
- Recurring transactions are working. Next, will add "Next Date" column to Recurring table (28/2)
- Recurring transactions are complete and now balances are in place as well (08/03)
- Category balances have been implemented (09/03)
- Previous and next month views have been implemented (13/03)
- Transfers have been implemented (13/03)
- Hid "Pay period" date filter (13/03)
- Added "Clone transaction" function, and fool-proofed the app by adding "Discard changes" messages and stopping updates that would crash the app (add transaction with no amount or description, delete account or category with existing transactions, adding a custom date filter where the end date is before the start date) (17/03)
- Translated strings to Portuguese and made the currency symbol flexible (17/03)