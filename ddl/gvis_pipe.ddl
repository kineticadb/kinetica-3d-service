CREATE TABLE "3DBASIN"."gvis_pipe"
(
    "project_id" VARCHAR (8) NOT NULL,
    "name" VARCHAR (32) NOT NULL,
    "stage" INTEGER,
    "md" REAL NOT NULL,
    "x" REAL NOT NULL,
    "y" REAL NOT NULL,
    "z" REAL NOT NULL
)
TIER STRATEGY (
( ( VRAM 1, RAM 5, PERSIST 5 ) )
);
