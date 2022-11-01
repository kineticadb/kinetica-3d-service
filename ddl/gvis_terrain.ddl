CREATE TABLE "3DBASIN"."gvis_terrain"
(
    "project_id" VARCHAR (32, dict),
    "lod" INTEGER NOT NULL,
    "x" REAL NOT NULL,
    "y" REAL NOT NULL,
    "z" REAL NOT NULL,
    "scan_x" INTEGER NOT NULL,
    "scan_y" INTEGER NOT NULL,
    "layer" INTEGER NOT NULL
)
TIER STRATEGY (
( ( VRAM 1, RAM 5, PERSIST 5 ) )
);

