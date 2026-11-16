-- Customer inquiries (leads). Contact details are customer-provided and treated as untrusted input.

CREATE TABLE leads (
    id                 UUID PRIMARY KEY,
    business_id        UUID          NOT NULL REFERENCES businesses (id) ON DELETE CASCADE,
    customer_name      VARCHAR(120)  NOT NULL,
    phone              VARCHAR(30),
    email              VARCHAR(254),
    message            VARCHAR(2000) NOT NULL,
    product_id         UUID REFERENCES products (id) ON DELETE SET NULL,
    product_name       VARCHAR(120),
    preferred_date     DATE,
    budget             VARCHAR(60),
    source             VARCHAR(20)   NOT NULL,
    status             VARCHAR(20)   NOT NULL,
    notes              VARCHAR(4000),
    quote_draft        VARCHAR(4000),
    status_changed_at  TIMESTAMPTZ   NOT NULL,
    created_at         TIMESTAMPTZ   NOT NULL,
    updated_at         TIMESTAMPTZ   NOT NULL,
    CONSTRAINT ck_leads_status CHECK (status IN ('NEW', 'CONTACTED', 'QUALIFIED', 'QUOTED', 'CONVERTED', 'LOST')),
    CONSTRAINT ck_leads_source CHECK (source IN ('WEBSITE', 'MANUAL')),
    CONSTRAINT ck_leads_contact CHECK (phone IS NOT NULL OR email IS NOT NULL)
);
CREATE INDEX ix_leads_business_created ON leads (business_id, created_at DESC);
CREATE INDEX ix_leads_business_status ON leads (business_id, status, created_at DESC);
