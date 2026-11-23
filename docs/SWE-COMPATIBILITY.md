# SWE compatibility notes

## Safety rules

- A successful candidate HMAC check is a container-integrity result, not proof of game compatibility.
- Never discard unknown root keys or unknown S0 groups.
- Do not infer coordinate scale, object IDs, field defaults, or array order from a single sample.
- A real export adapter requires sample-driven fixtures and an in-game import test.

## Candidate container

The code currently recognizes a text container consisting of Base64 JSON followed by a 40-character lowercase/uppercase hexadecimal HMAC-SHA1 digest. The key recorded in prior reverse-engineering notes is `2559F35097-2021`. Both assumptions require regression against known-good files for each target game version.

## Editing status

The editor project format is fully separate from SWE. Importing an SWE currently verifies and parses the container in read-only mode. It intentionally does not offer a misleading export button until terrain/object schemas, coordinate units, metadata mapping, and checksum placement have been confirmed with known-good fixtures.
