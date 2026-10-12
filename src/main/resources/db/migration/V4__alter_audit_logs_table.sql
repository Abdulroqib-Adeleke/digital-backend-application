ALTER TABLE audit_logs
DROP CONSTRAINT IF EXISTS audit_logs_action_type_check;

ALTER TABLE audit_logs
    ADD CONSTRAINT audit_logs_action_type_check
        CHECK (action_type IN (
                               'USER_REGISTRATION', 'USER_LOGIN', 'ACCOUNT_CREATED', 'SECONDARY_ACCOUNT_CREATED',
                               'ACCOUNT_SUSPENDED', 'ACCOUNT_UNSUSPENDED', 'KYC_SUBMITTED', 'KYC_APPROVED',
                               'KYC_REJECTED', 'TIER_UPGRADED', 'TRANSACTION_INITIATED', 'TRANSACTION_SUCCESS',
                               'TRANSACTION_FAILED', 'TRANSACTION_REVERSED', 'USER_PROFILE_FETCHED',
                               'PENDING_KYC_FETCHED', 'TRANSACTION_FETCHED', 'BANK_OVERVIEW', 'ADMIN_REGISTRATION',
                               'PASSWORD_CHANGED', 'ANOTHER_ACCESS_TOKEN', 'DEBIT_TRANSACTION_SUCCESS',
                               'SELF_CREDIT_TRANSACTION_SUCCESS', 'TRANSACTION_REQUERIED', 'USER_LOGOUT',
                               'STATEMENT_GENERATED', 'ACCOUNT_DELETED', 'ADMIN_LOGIN'
                              )
            );