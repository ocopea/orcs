-- Creating applicationTemplate table
create table siteConfig (
    data JSONB NOT NULL
);

create table deployedApplicationEvent (
    appInstanceId uuid NOT NULL,
    version bigint NOT NULL,
    data JSONB NOT NULL
);

-- ALTER TABLE deployedApplicationEvent ADD CONSTRAINT uk_deployedApplicationEvent_instance_version UNIQUE (appInstanceId, version);
-- CREATE INDEX idx_deployedApplicationEvent_appInstanceId ON deployedApplicationEvent (appInstanceId);
