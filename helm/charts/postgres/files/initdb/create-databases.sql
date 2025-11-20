\connect bank

-- Создание схем
CREATE SCHEMA IF NOT EXISTS accounts;
CREATE SCHEMA IF NOT EXISTS cash;
CREATE SCHEMA IF NOT EXISTS transfer;
CREATE SCHEMA IF NOT EXISTS exchange;
CREATE SCHEMA IF NOT EXISTS blocker;
CREATE SCHEMA IF NOT EXISTS notifications;
CREATE SCHEMA IF NOT EXISTS keycloak;

-- Создание ролей (если ещё не существуют)
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'accounts_service_user') THEN
CREATE ROLE accounts_service_user LOGIN PASSWORD 'accounts_service_password';
END IF;

  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'cash_service_user') THEN
CREATE ROLE cash_service_user LOGIN PASSWORD 'cash_service_password';
END IF;

  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'transfer_service_user') THEN
CREATE ROLE transfer_service_user LOGIN PASSWORD 'transfer_service_password';
END IF;

  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'exchange_service_user') THEN
CREATE ROLE exchange_service_user LOGIN PASSWORD 'exchange_service_password';
END IF;

  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'blocker_service_user') THEN
CREATE ROLE blocker_service_user LOGIN PASSWORD 'blocker_service_password';
END IF;

  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'notifications_service_user') THEN
CREATE ROLE notifications_service_user LOGIN PASSWORD 'notifications_service_password';
END IF;

  -- Важно: здесь имя роли без суффикса, как у тебя в конфиге
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'keycloak') THEN
CREATE ROLE keycloak LOGIN PASSWORD 'keycloak_password';
END IF;
END
$$;

-- Права на схемы
GRANT ALL ON SCHEMA accounts       TO accounts_service_user;
GRANT ALL ON SCHEMA cash           TO cash_service_user;
GRANT ALL ON SCHEMA transfer       TO transfer_service_user;
GRANT ALL ON SCHEMA exchange       TO exchange_service_user;
GRANT ALL ON SCHEMA blocker        TO blocker_service_user;
GRANT ALL ON SCHEMA notifications  TO notifications_service_user;
GRANT ALL ON SCHEMA keycloak       TO keycloak;  -- для keycloak оставляем роль 'keycloak'

-- Права на существующие таблицы и последовательности в схемах
DO $$
DECLARE
sch text;
  grantee text;
BEGIN
FOR sch IN
SELECT schema_name
FROM information_schema.schemata
WHERE schema_name IN ('accounts','cash','transfer','exchange','blocker','notifications','keycloak')
    LOOP
    -- Подбираем имя роли-получателя прав
    grantee := CASE
                 WHEN sch = 'keycloak' THEN 'keycloak'
                 ELSE sch || '_service_user'
END;

EXECUTE format('GRANT ALL ON ALL TABLES IN SCHEMA %I TO %I', sch, grantee);
EXECUTE format('GRANT ALL ON ALL SEQUENCES IN SCHEMA %I TO %I', sch, grantee);
END LOOP;
END
$$;

-- (Опционально) Чтобы сервисы могли создавать объекты в своих схемах
-- (ALL ON SCHEMA уже включает CREATE и USAGE, но оставляю явным образом — не повредит)
GRANT USAGE, CREATE ON SCHEMA accounts      TO accounts_service_user;
GRANT USAGE, CREATE ON SCHEMA cash          TO cash_service_user;
GRANT USAGE, CREATE ON SCHEMA transfer      TO transfer_service_user;
GRANT USAGE, CREATE ON SCHEMA exchange      TO exchange_service_user;
GRANT USAGE, CREATE ON SCHEMA blocker       TO blocker_service_user;
GRANT USAGE, CREATE ON SCHEMA notifications TO notifications_service_user;
GRANT USAGE, CREATE ON SCHEMA keycloak      TO keycloak;
