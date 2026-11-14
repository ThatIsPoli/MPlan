CREATE TABLE IF NOT EXISTS CONTATTO (
    id              VARCHAR(36)  NOT NULL PRIMARY KEY,
    nome            VARCHAR(100) NOT NULL,
    cognome         VARCHAR(100),
    indirizzo       VARCHAR(200),
    codice_fiscale  VARCHAR(16),
    partita_iva     VARCHAR(11),
    tipo            VARCHAR(20),
    CONSTRAINT chk_contatto_tipo
        CHECK (tipo IS NULL OR tipo IN ('PRIVATO', 'AZIENDA'))
);

CREATE TABLE IF NOT EXISTS TELEFONO_CONTATTO (
    id_contatto     VARCHAR(36)  NOT NULL,
    numero          VARCHAR(20)  NOT NULL,
    PRIMARY KEY (id_contatto, numero),
    CONSTRAINT fk_telefono_contatto
        FOREIGN KEY (id_contatto) REFERENCES CONTATTO (id)
);

CREATE TABLE IF NOT EXISTS ATTIVITA (
    id                  VARCHAR(36)   NOT NULL PRIMARY KEY,
    id_contatto         VARCHAR(36)   NOT NULL,
    titolo              VARCHAR(100)  NOT NULL,
    data                VARCHAR(10)   NOT NULL,
    ora                 VARCHAR(8),
    descrizione         VARCHAR(5000),
    note                VARCHAR(5000),
    stato               VARCHAR(20)   NOT NULL,
    motivo_annullamento VARCHAR(1000),
    CONSTRAINT chk_attivita_stato
        CHECK (stato IN ('DA_FARE', 'PARZIALE', 'COMPLETATA', 'ANNULLATA')),
    CONSTRAINT fk_attivita_contatto
        FOREIGN KEY (id_contatto) REFERENCES CONTATTO (id)
);

-- Indici per rispettare NFR.3 / Q5 (< 2 secondi con 5.000+ attivita)
CREATE INDEX IF NOT EXISTS idx_attivita_contatto_data
    ON ATTIVITA (id_contatto, data);

CREATE INDEX IF NOT EXISTS idx_attivita_data
    ON ATTIVITA (data);

CREATE TABLE IF NOT EXISTS VOCE_REGISTRO (
    id              INTEGER       PRIMARY KEY,
    id_attivita     VARCHAR(36)   NOT NULL,
    quando          VARCHAR(30)   NOT NULL,
    tipo            VARCHAR(20)   NOT NULL,
    motivo          VARCHAR(1000),
    CONSTRAINT chk_voce_registro_tipo
        CHECK (tipo IN ('MODIFICA', 'CAMBIO_STATO', 'ANNULLAMENTO')),
    CONSTRAINT fk_voce_registro_attivita
        FOREIGN KEY (id_attivita) REFERENCES ATTIVITA (id)
);

CREATE INDEX IF NOT EXISTS idx_voce_registro_attivita
    ON VOCE_REGISTRO (id_attivita, quando);