-- Foundation: identity, tenants (businesses), membership and audit.
-- Every tenant-owned table carries business_id and is indexed by it.

CREATE TABLE users (
    id              UUID PRIMARY KEY,
    email           VARCHAR(254) NOT NULL,
    password_hash   VARCHAR(100) NOT NULL,
    full_name       VARCHAR(120) NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL
);
-- Emails are stored lower-cased by the application; the unique index enforces it.
CREATE UNIQUE INDEX ux_users_email ON users (email);

CREATE TABLE businesses (
    id                 UUID PRIMARY KEY,
    slug               VARCHAR(64)  NOT NULL,
    name               VARCHAR(120) NOT NULL,
    category           VARCHAR(40)  NOT NULL,
    tagline            VARCHAR(200),
    description        TEXT,
    city               VARCHAR(80),
    area               VARCHAR(120),
    address            VARCHAR(300),
    phone              VARCHAR(30),
    whatsapp           VARCHAR(30),
    email              VARCHAR(254),
    target_customers   VARCHAR(300),
    languages          VARCHAR(120),
    opening_hours      JSONB,
    plan               VARCHAR(20)  NOT NULL DEFAULT 'FREE',
    setup_stage        VARCHAR(30)  NOT NULL DEFAULT 'PROFILE_DRAFT',
    created_at         TIMESTAMPTZ  NOT NULL,
    updated_at         TIMESTAMPTZ  NOT NULL,
    CONSTRAINT ck_businesses_plan CHECK (plan IN ('FREE', 'STARTER', 'GROWTH', 'BUSINESS'))
);
CREATE UNIQUE INDEX ux_businesses_slug ON businesses (slug);

CREATE TABLE business_members (
    business_id  UUID        NOT NULL REFERENCES businesses (id) ON DELETE CASCADE,
    user_id      UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role         VARCHAR(20) NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (business_id, user_id),
    CONSTRAINT ck_business_members_role CHECK (role IN ('OWNER', 'STAFF'))
);
CREATE INDEX ix_business_members_user ON business_members (user_id);

CREATE TABLE audit_logs (
    id             UUID PRIMARY KEY,
    business_id    UUID REFERENCES businesses (id) ON DELETE CASCADE,
    actor_user_id  UUID REFERENCES users (id) ON DELETE SET NULL,
    actor_type     VARCHAR(20)  NOT NULL,
    action         VARCHAR(80)  NOT NULL,
    entity_type    VARCHAR(40),
    entity_id      VARCHAR(64),
    details        JSONB,
    created_at     TIMESTAMPTZ  NOT NULL
);
CREATE INDEX ix_audit_logs_business_created ON audit_logs (business_id, created_at DESC);
