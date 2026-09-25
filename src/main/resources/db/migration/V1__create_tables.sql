
CREATE TABLE users (
                       id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                       first_name      varchar(50) NOT NULL,
                       last_name       varchar(50) NOT NULL,
                       email           varchar(50) UNIQUE NOT NULL,
                       password        varchar(65) NOT NULL,
                       phone_number    varchar(25) UNIQUE NOT NULL,
                       gender          varchar(25) NOT NULL
                           CHECK ( gender IN ('MALE', 'FEMALE') ),
                       date_of_birth   date NOT NULL,
                       role            varchar(25) NOT NULL
                           CHECK ( role IN ('SYS_ADMIN', 'ADMIN', 'CUSTOMER') ),
                       created_at      timestamp NOT NULL DEFAULT now(),
                       updated_at      timestamp NOT NULL DEFAULT now()
);

CREATE TABLE admin (
                       user_id     uuid PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                       admin_id   varchar(255) NOT NULL,
                       address     varchar(255) NOT NULL,
                       is_admin_active    boolean

);

CREATE TABLE customers (
                           id              uuid PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                           transaction_pin varchar(255),
                           address         varchar(100) NOT NULL,
                           nin             varchar(100) UNIQUE,
                           bvn             varchar(100) UNIQUE
);

CREATE TABLE accounts (
                          id                      uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                          owner_id                uuid NOT NULL REFERENCES customers(id),
                          account_status          varchar(25) NOT NULL
                              CHECK ( account_status IN ('PENDING_VERIFICATION', 'ACTIVE', 'DORMANT', 'FROZEN') ),
                          account_number          varchar(25) UNIQUE NOT NULL,
                          balance                 numeric(19, 2) NOT NULL,
                          account_tier            varchar(25) NOT NULL
                              CHECK ( account_tier IN ('TIER_1', 'TIER_2', 'TIER_3') ),
                          account_type            varchar(25)
                              CHECK ( account_type IN ('SAVINGS', 'CHECKINGS', 'RETIREMENTS', 'CDS') ),
                          created_at              timestamp NOT NULL DEFAULT now(),
                          updated_at              timestamp NOT NULL DEFAULT now()
);

CREATE INDEX idx_accounts_owner_id ON accounts(owner_id);

CREATE TABLE transactions (
                              id                          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                              transaction_type            varchar(25) NOT NULL
                                  CHECK ( transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER') ),
                              transaction_status          varchar(25) NOT NULL
                                  CHECK ( transaction_status IN ('DECLINED', 'PENDING', 'SUCCESSFUL') ),
                              source_account              varchar(25) REFERENCES accounts(account_number),
                              source_account_name         varchar(255),
                              destination_account         varchar(25) REFERENCES accounts(account_number),
                              destination_account_number  varchar(255),
                              "Destination_account_name"  varchar(255),
                              amount_transferred          numeric(19, 2) NOT NULL,
                              description                 varchar(50) NOT NULL,
                              balance_after_transaction   numeric(19, 2),
                              created_at                  timestamp NOT NULL DEFAULT now(),
                              updated_at                  timestamp NOT NULL DEFAULT now()
);

CREATE INDEX idx_transactions_source_account ON transactions(source_account);
CREATE INDEX idx_transactions_destination_account ON transactions(destination_account);

CREATE TABLE ledger_entries (
                                id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                account_id      uuid REFERENCES accounts(id),
                                transaction_id  uuid NOT NULL REFERENCES transactions(id),
                                entry_type      varchar(25) NOT NULL
                                    CHECK ( entry_type IN ('DEBIT', 'CREDIT') ),
                                status          varchar(25) NOT NULL
                                    CHECK ( status IN ('SETTLED', 'PENDING', 'VOID') ),
                                amount          numeric(19, 2) NOT NULL,
                                created_at      timestamp NOT NULL DEFAULT now(),
                                settled_at      timestamp,
                                voided_at       timestamp,
                                updated_at      timestamp NOT NULL DEFAULT now()
);

CREATE INDEX idx_ledger_entries_account_id ON ledger_entries(account_id);
CREATE INDEX idx_ledger_entries_transaction_id ON ledger_entries(transaction_id);

CREATE TABLE account_daily_audit (
                                     id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                     "accountId"     uuid REFERENCES accounts(id),
                                     opening_amount  numeric(19, 2),
                                     closing_amount  numeric(19, 2),
                                     date            date
);

CREATE INDEX idx_account_daily_audit_account_date ON account_daily_audit("accountId", date);

CREATE TABLE audit_logs (
                            id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                            user_id             uuid NOT NULL,
                            user_email          varchar(255) NOT NULL,
                            action_type         varchar(100) NOT NULL
                                CHECK ( action_type IN (
                                                        'USER_REGISTRATION','USER_LOGIN','ACCOUNT_CREATED','SECONDARY_ACCOUNT_CREATED',
                                                        'ACCOUNT_SUSPENDED','ACCOUNT_UNSUSPENDED','KYC_SUBMITTED','KYC_APPROVED','KYC_REJECTED',
                                                        'TIER_UPGRADED','TRANSACTION_INITIATED','TRANSACTION_SUCCESS','TRANSACTION_FAILED',
                                                        'TRANSACTION_REVERSED','USER_PROFILE_FETCHED','PENDING_KYC_FETCHED','TRANSACTION_FETCHED',
                                                        'BANK_OVERVIEW','ADMIN_REGISTRATION','PASSWORD_CHANGED','ANOTHER_ACCESS_TOKEN',
                                                        'DEBIT_TRANSACTION_SUCCESS','SELF_CREDIT_TRANSACTION_SUCCESS','TRANSACTION_REQUERIED',
                                                        'USER_LOGOUT','STATEMENT_GENERATED'
                                    ) ),
                            entity_type         varchar(255) NOT NULL,
                            time_of_creation    timestamp NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);


CREATE TABLE daily_transactions (
                                    id                  BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
                                    total_credit        numeric(19, 2) NOT NULL,
                                    total_debit         numeric(19, 2) NOT NULL,
                                    transaction_date    date NOT NULL UNIQUE
);


CREATE TABLE daily_transfer_totals (
                                       id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                       account_number  varchar(25) NOT NULL,
                                       transfer_date   date NOT NULL,
                                       total_amount    numeric(19, 2) NOT NULL
);

CREATE INDEX idx_daily_transfer_totals_account_date ON daily_transfer_totals(account_number, transfer_date);


CREATE TABLE kyc_entities (
                              id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                              customer_id         uuid,
                              account_id          uuid,
                              document_type       varchar(25) NOT NULL
                                  CHECK ( document_type IN ('NIN', 'BVN') ),
                              submitted_value     varchar(25),
                              status              varchar(25) NOT NULL
                                  CHECK ( status IN ('PENDING', 'APPROVED', 'REJECTED') ),
                              rejection_reason    varchar(100),
                              upgraded_to         varchar(25)
                                  CHECK ( upgraded_to IN ('TIER_1', 'TIER_2', 'TIER_3') ),
                              submitted_at        timestamp NOT NULL DEFAULT now(),
                              resolved_at         timestamp NOT NULL
);

CREATE INDEX idx_kyc_entities_customer_id ON kyc_entities(customer_id);
CREATE INDEX idx_kyc_entities_account_id ON kyc_entities(account_id);


CREATE TABLE login_sessions (
                                id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                user_id             uuid,
                                active_session_id   varchar(255),
                                logged_in           boolean,
                                time_of_log_in      timestamp,
                                time_of_log_out     timestamp
);

CREATE INDEX idx_login_sessions_user_id ON login_sessions(user_id);


CREATE TABLE otp_verifications (
                                   id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                   customer_id     uuid NOT NULL,
                                   account_number  varchar(255) NOT NULL,
                                   otp_hash        varchar(255) NOT NULL,
                                   expires_at      timestamp NOT NULL,
                                   verified_at     timestamp,
                                   attempt_count   integer NOT NULL DEFAULT 0,
                                   channel         varchar(25) NOT NULL
                                       CHECK ( channel IN ('EMAIL', 'SMS') ),
                                   created_at      timestamp NOT NULL DEFAULT now()
);

CREATE INDEX idx_otp_verifications_customer_id ON otp_verifications(customer_id);
CREATE INDEX idx_otp_verifications_account_number ON otp_verifications(account_number);


CREATE TABLE refresh_sessions (
                                  id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
                                  active_session_id   varchar(50),
                                  hashed_token        varchar(255),
                                  user_id             uuid,
                                  expiry_time         timestamp,
                                  created_date        timestamp NOT NULL DEFAULT now(),
                                  updated_date         timestamp NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_sessions_user_id ON refresh_sessions(user_id);
CREATE INDEX idx_refresh_sessions_token ON refresh_sessions(hashed_token);
