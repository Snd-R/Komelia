CREATE TABLE BookColorCorrection
(
    book_id TEXT NOT NULL,
    type    TEXT NOT NULL,
    PRIMARY KEY (book_id)
);
CREATE TABLE BookColorCurves
(
    book_id            TEXT NOT NULL,
    color_curve_points TEXT NOT NULL,
    red_curve_points   TEXT NOT NULL,
    green_curve_points TEXT NOT NULL,
    blue_curve_points  TEXT NOT NULL,

    PRIMARY KEY (book_id),
    FOREIGN KEY (book_id) REFERENCES BookColorCorrection (book_id)
);

CREATE TABLE BookColorLevels
(
    book_id        TEXT NOT NULL,
    color_low_in   REAL NOT NULL,
    color_high_in  REAL NOT NULL,
    color_low_out  REAL NOT NULL,
    color_high_out REAL NOT NULL,
    color_gamma    REAL NOT NULL,
    red_low_in     REAL NOT NULL,
    red_high_in    REAL NOT NULL,
    red_low_out    REAL NOT NULL,
    red_high_out   REAL NOT NULL,
    red_gamma      REAL NOT NULL,
    green_low_in   REAL NOT NULL,
    green_high_in  REAL NOT NULL,
    green_low_out  REAL NOT NULL,
    green_high_out REAL NOT NULL,
    green_gamma    REAL NOT NULL,
    blue_low_in    REAL NOT NULL,
    blue_high_in   REAL NOT NULL,
    blue_low_out   REAL NOT NULL,
    blue_high_out  REAL NOT NULL,
    blue_gamma     REAL NOT NULL,
    PRIMARY KEY (book_id),
    FOREIGN KEY (book_id) REFERENCES BookColorCorrection (book_id)
);

CREATE TABLE ColorCurvePresets
(
    name               TEXT NOT NULL,
    color_curve_points TEXT NOT NULL,
    red_curve_points   TEXT NOT NULL,
    green_curve_points TEXT NOT NULL,
    blue_curve_points  TEXT NOT NULL,
    PRIMARY KEY (name)
);


CREATE TABLE ColorLevelsPresets
(
    name           TEXT NOT NULL,
    color_low_in   REAL NOT NULL,
    color_high_in  REAL NOT NULL,
    color_low_out  REAL NOT NULL,
    color_high_out REAL NOT NULL,
    color_gamma    REAL NOT NULL,
    red_low_in     REAL NOT NULL,
    red_high_in    REAL NOT NULL,
    red_low_out    REAL NOT NULL,
    red_high_out   REAL NOT NULL,
    red_gamma      REAL NOT NULL,
    green_low_in   REAL NOT NULL,
    green_high_in  REAL NOT NULL,
    green_low_out  REAL NOT NULL,
    green_high_out REAL NOT NULL,
    green_gamma    REAL NOT NULL,
    blue_low_in    REAL NOT NULL,
    blue_high_in   REAL NOT NULL,
    blue_low_out   REAL NOT NULL,
    blue_high_out  REAL NOT NULL,
    blue_gamma     REAL NOT NULL,
    PRIMARY KEY (name)
);
