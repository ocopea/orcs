CREATE SCHEMA blob_store;

create TABLE blob_store.objects (
  f_id SERIAL ,
  f_namespace VARCHAR(1024),
  f_key VARCHAR(1024),
  f_headers JSON,
  f_lastmodified BIGINT NOT NULL,
  f_retention BIGINT,
  f_value OID,
  CONSTRAINT pk_objects_id PRIMARY KEY (f_id)
);

CREATE UNIQUE INDEX idx_unique_objects_namespace_key on blob_store.objects(f_namespace, f_key);

