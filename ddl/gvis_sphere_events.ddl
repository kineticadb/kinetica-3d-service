CREATE TABLE "3DBASIN"."gvis_sphere_events_new"
(
    "project_id" VARCHAR (32, dict) NOT NULL,
    "event_id" VARCHAR (shard_key, 32, dict) NOT NULL,
    "event_ts" TIMESTAMP,
    "x" REAL,
    "y" REAL,
    "z" REAL,
    "amp" REAL,
    "color_id" INTEGER
)
TIER STRATEGY (
( ( VRAM 1, RAM 5, PERSIST 5 ) )
);

