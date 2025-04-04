ALTER TABLE task 
ADD COLUMN task_status VARCHAR(16)
DEFAULT 'Active'
CHECK (task_status IN ('Active', 'Completed', 'Overdue', 'Late'));