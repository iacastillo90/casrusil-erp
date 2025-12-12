-- Add metadata columns to accounting_entries table in accounting schema
-- Using IF NOT EXISTS to avoid errors if columns were added by ddl-auto

ALTER TABLE accounting.accounting_entries ADD COLUMN IF NOT EXISTS tax_payer_id VARCHAR(20);
ALTER TABLE accounting.accounting_entries ADD COLUMN IF NOT EXISTS tax_payer_name VARCHAR(255);
ALTER TABLE accounting.accounting_entries ADD COLUMN IF NOT EXISTS document_type VARCHAR(10);
ALTER TABLE accounting.accounting_entries ADD COLUMN IF NOT EXISTS document_number VARCHAR(50);
ALTER TABLE accounting.accounting_entries ADD COLUMN IF NOT EXISTS status VARCHAR(20);

-- Create index for fast searching by RUT and Folio (Crucial for audits)
CREATE INDEX IF NOT EXISTS idx_entries_search ON accounting.accounting_entries(tax_payer_id, document_number);
