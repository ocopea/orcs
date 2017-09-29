create table applicationCopyEvent (
    appCopyId uuid NOT NULL,
    appInstanceId uuid NOT NULL,
    version bigint NOT NULL,
    data JSONB NOT NULL
);
