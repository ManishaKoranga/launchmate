-- Catalogue, websites (draft + immutable publications), generation jobs and AI conversations.

CREATE TABLE products (
    id            UUID PRIMARY KEY,
    business_id   UUID          NOT NULL REFERENCES businesses (id) ON DELETE CASCADE,
    kind          VARCHAR(20)   NOT NULL,
    name          VARCHAR(120)  NOT NULL,
    description   VARCHAR(1000),
    price_amount  NUMERIC(12, 2),
    price_note    VARCHAR(60),
    category      VARCHAR(60),
    image_key     VARCHAR(200),
    visible       BOOLEAN       NOT NULL DEFAULT TRUE,
    sort_order    INT           NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ   NOT NULL,
    updated_at    TIMESTAMPTZ   NOT NULL,
    CONSTRAINT ck_products_kind CHECK (kind IN ('PRODUCT', 'SERVICE')),
    CONSTRAINT ck_products_price CHECK (price_amount IS NULL OR price_amount >= 0)
);
CREATE INDEX ix_products_business_sort ON products (business_id, sort_order, created_at);

-- One website per business. draft_config is freely editable; the public site only ever reads
-- the publication referenced by published_publication_id.
CREATE TABLE websites (
    id                        UUID PRIMARY KEY,
    business_id               UUID        NOT NULL REFERENCES businesses (id) ON DELETE CASCADE,
    template_id               VARCHAR(40) NOT NULL,
    draft_config              JSONB       NOT NULL,
    draft_revision            INT         NOT NULL DEFAULT 1,
    draft_source              VARCHAR(20) NOT NULL,
    published_publication_id  UUID,
    created_at                TIMESTAMPTZ NOT NULL,
    updated_at                TIMESTAMPTZ NOT NULL
);
CREATE UNIQUE INDEX ux_websites_business ON websites (business_id);

CREATE TABLE publications (
    id            UUID PRIMARY KEY,
    business_id   UUID        NOT NULL REFERENCES businesses (id) ON DELETE CASCADE,
    website_id    UUID        NOT NULL REFERENCES websites (id) ON DELETE CASCADE,
    version       INT         NOT NULL,
    config        JSONB       NOT NULL,
    published_by  UUID REFERENCES users (id) ON DELETE SET NULL,
    created_at    TIMESTAMPTZ NOT NULL,
    CONSTRAINT ux_publications_website_version UNIQUE (website_id, version)
);
CREATE INDEX ix_publications_business ON publications (business_id, created_at DESC);

ALTER TABLE websites
    ADD CONSTRAINT fk_websites_published FOREIGN KEY (published_publication_id) REFERENCES publications (id);

-- Long-running website generation with persisted per-step progress.
CREATE TABLE generation_jobs (
    id             UUID PRIMARY KEY,
    business_id    UUID         NOT NULL REFERENCES businesses (id) ON DELETE CASCADE,
    requested_by   UUID REFERENCES users (id) ON DELETE SET NULL,
    mode           VARCHAR(20)  NOT NULL,
    status         VARCHAR(20)  NOT NULL,
    steps          JSONB        NOT NULL,
    error_message  VARCHAR(500),
    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL,
    completed_at   TIMESTAMPTZ,
    CONSTRAINT ck_generation_jobs_status CHECK (status IN ('QUEUED', 'RUNNING', 'SUCCEEDED', 'FAILED'))
);
CREATE INDEX ix_generation_jobs_business ON generation_jobs (business_id, created_at DESC);

-- AI conversations. business_id is NULL only for onboarding conversations that started before the
-- business existed; they are always scoped to their user.
CREATE TABLE conversations (
    id           UUID PRIMARY KEY,
    user_id      UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    business_id  UUID REFERENCES businesses (id) ON DELETE CASCADE,
    kind         VARCHAR(20)  NOT NULL,
    state        JSONB,
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL,
    CONSTRAINT ck_conversations_kind CHECK (kind IN ('ONBOARDING', 'ASSISTANT'))
);
CREATE INDEX ix_conversations_user ON conversations (user_id, kind, updated_at DESC);
CREATE INDEX ix_conversations_business ON conversations (business_id, kind, updated_at DESC);

CREATE TABLE conversation_messages (
    id               UUID PRIMARY KEY,
    conversation_id  UUID         NOT NULL REFERENCES conversations (id) ON DELETE CASCADE,
    role             VARCHAR(20)  NOT NULL,
    content          TEXT         NOT NULL,
    metadata         JSONB,
    created_at       TIMESTAMPTZ  NOT NULL,
    CONSTRAINT ck_conversation_messages_role CHECK (role IN ('USER', 'ASSISTANT', 'TOOL'))
);
CREATE INDEX ix_conversation_messages_conversation ON conversation_messages (conversation_id, created_at);
