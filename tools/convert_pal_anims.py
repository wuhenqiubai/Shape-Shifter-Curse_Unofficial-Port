#!/usr/bin/env python3
"""
Convert all PAL animation JSON files into a single GeckoLib-compatible
merged .animation.json file. Handles bone name remapping and
body+torso->bipedBody merging.
"""

import json
import os
from pathlib import Path

BONE_MAP = {
    "head": "bipedHead",
    "body": "bipedBody",
    "torso": "bipedBody",
    "rightArm": "bipedRightArm",
    "leftArm": "bipedLeftArm",
    "rightLeg": "bipedRightLeg",
    "leftLeg": "bipedLeftLeg",
    "bipedLeftHindLeg": "bipedLeftHindLeg",
    "bipedRightHindLeg": "bipedRightHindLeg",
}

SRC_DIR = Path("src/main/resources/assets/shape-shifter-curse/player_animations")
TGT_DIR = Path("src/main/resources/assets/shape-shifter-curse/animations")
TGT_FILE = "form_animations.animation.json"


def merge_body_torso(bones: dict) -> dict:
    """Merge PAL 'body' and 'torso' into single 'bipedBody' bone."""
    result = {}
    body_data = None
    torso_data = None

    for bone_name, channels in bones.items():
        geo_name = BONE_MAP.get(bone_name)
        if geo_name is None:
            print(f"  WARNING: Unknown bone '{bone_name}', skipping")
            continue

        if bone_name == "body":
            body_data = channels
            continue
        if bone_name == "torso":
            torso_data = channels
            continue

        if geo_name in result:
            result[geo_name].update(channels)
        else:
            result[geo_name] = channels

    merged = {}
    if body_data:
        if "position" in body_data:
            merged["position"] = body_data["position"]
        if "rotation" in body_data:
            merged["rotation"] = body_data["rotation"]
    if torso_data:
        if "rotation" in torso_data:
            merged["rotation"] = torso_data["rotation"]
        if "position" in torso_data:
            merged["position"] = torso_data["position"]

    if merged:
        result["bipedBody"] = merged

    return result


def main():
    os.makedirs(TGT_DIR, exist_ok=True)

    src_files = sorted(SRC_DIR.glob("*.json"))
    all_animations = {}
    converted = 0
    skipped = 0

    for src_path in src_files:
        with open(src_path, "r", encoding="utf-8") as f:
            data = json.load(f)

        if "animations" not in data:
            print(f"SKIP: {src_path.name} (no 'animations' key)")
            skipped += 1
            continue

        for anim_name, anim_data in data["animations"].items():
            if "bones" in anim_data:
                anim_data = dict(anim_data)
                anim_data["bones"] = merge_body_torso(anim_data["bones"])
            all_animations[anim_name] = anim_data
            converted += 1

    output = {
        "format_version": "1.8.0",
        "geckolib_format_version": 2,
        "animations": all_animations,
    }

    tgt_path = TGT_DIR / TGT_FILE
    with open(tgt_path, "w", encoding="utf-8") as f:
        json.dump(output, f, indent=2)

    # Clean up individual files from previous run
    old_dir = TGT_DIR / "form"
    if old_dir.exists():
        import shutil
        shutil.rmtree(old_dir)

    print(f"\nDone. {converted} animations from {len(src_files)} source files -> {tgt_path}")
    print(f"Skipped: {skipped} files")
    print(f"Total animations in output: {len(all_animations)}")


if __name__ == "__main__":
    main()
