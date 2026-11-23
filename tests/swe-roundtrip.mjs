import assert from "node:assert/strict";
import { encodeSwe, inspectSwe, hmacSha1 } from "../core/swe.js";

assert.equal(hmacSha1("test"), "5f82b126ea273a1b9e15f762d7bfc9191441c13c");\n\nconst fixture = {
  S0: {
    S1: [{ user: "test-author", desc: "round trip", timer: 300 }],
    S2: [{ xx: 0, yy: 16, i: 49 }, { xx: 16, yy: 16, i: 56 }],
    S3: [{ ID: "obj_decoracion_1", spr: "spr_decoracion_1", xx: 32, yy: 16, i: 1 }],
    S4: [{ ID: "obj_pinchos_res", xx: 48, yy: 16, key: 0, rot: 0, scl: 1 }],
    S5: [{ xx: 64, yy: 16, xscl: 1, yscl: 1 }],
    S6: [],
    S7: [],
    S8: [{ ID: "obj_door_res", xx: 80, yy: 16, dr_x: 96, dr_y: 32 }]
  },
  SB1: { S1: [] },
};

const blob = encodeSwe(fixture);
const parsed = await inspectSwe(await blob.arrayBuffer());
assert.equal(parsed.verified, true);
assert.deepEqual(parsed.data, fixture);
assert.equal(parsed.terrainCount, 2);
console.log("SWE container HMAC + JSON round-trip passed.");
