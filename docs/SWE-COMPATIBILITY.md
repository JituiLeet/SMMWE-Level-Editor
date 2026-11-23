# SWE compatibility notes

## Verified against a real SMMWE sample

The repository's `JituiLeet/Test/YINBI.swe` file was fetched and decoded as a known-good sample. Its outer representation is Base64-encoded JSON followed by a 40-character lowercase HMAC-SHA1 digest.

- HMAC-SHA1 key: `2559F35097-2021`
- Sample digest: `e629432d044575abcae517d4c42fa935d554ab46`
- The digest was independently recomputed and matched the sample.
- Root keys: `S0`, `SB1`.
- `S0.S1`: one level metadata record. Observed fields include `user`, `desc`, `goal_x`, `goal_y`, `start_y`, `size`, `gamestyle`, `gametheme`, and `timer`.
- `S0.S2`: 503 terrain records; each observed record contains `xx`, `yy`, and numeric `i`. Coordinates in the sample use 16-unit increments.
- `S0.S3`: 7 decoration records; observed fields include `ID`, `spr`, `xx`, `yy`, and `i`.
- `S0.S4`: 135 object records; observed fields include `ID`, `xx`, `yy`, and object-specific properties.
- `S0.S5`: 15 platform records with many transform/size fields and `xx`, `yy`.
- `S0.S6`: empty array.
- `S0.S7`: empty array.
- `S0.S8`: 3 door records with `ID`, `xx`, `yy`, `dr_x`, `dr_y`.
- `SB1.S1`: empty array in this sample.

## Editing implementation

The editor now:
- verifies the sample's HMAC-SHA1 before loading;
- edits S2 terrain records using 16-unit grid coordinates and numeric tile IDs;
- clones known source records when placing S3/S4/S5/S8 records, preserving extra fields;
- preserves untouched root keys and data groups;
- recalculates HMAC-SHA1 after export;
- keeps SWE documents embedded in the editor project file.

## Remaining compatibility caveat

The sample establishes the record shapes and checksum for this file, not the meaning of every numeric terrain ID or every optional object property. The editor therefore presents numeric IDs and source-derived object IDs rather than inventing semantic labels. The exported file still requires an in-game import test for each target SMMWE version.
