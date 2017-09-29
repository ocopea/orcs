CREATE TABLE config_data (
    config_path CHARACTER VARYING(1024) NOT NULL,
    config_content TEXT NOT NULL,
    PRIMARY KEY(config_path)
);

