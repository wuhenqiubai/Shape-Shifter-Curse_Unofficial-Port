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
    "body": "bodyRoot",      # body-level transform → applied to MatrixStack, not a GeoBone
    "torso": "bipedBody",    # upper body → GeoBone bipedBody
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


def add_vectors(a, b):
    """Add two keyframe vector values. Both must be single vectors [x,y,z]."""
    return [a[0] + b[0], a[1] + b[1], a[2] + b[2]]


def _is_vector_dict(d):
    """Check if dict is a simple vector: {'vector': [x,y,z]}"""
    return isinstance(d, dict) and "vector" in d

def _get_vector_at_time(d, t):
    """Get the interpolated vector from a keyframe dict at time t.
    d can be a simple vector dict or a time-keyed dict."""
    if _is_vector_dict(d):
        return d["vector"]
    # Time-keyed: find the nearest keyframes and interpolate
    times = sorted([float(k) for k in d.keys()])
    if not times:
        return [0, 0, 0]
    # Find surrounding keyframes
    before_t, after_t = None, None
    before_v, after_v = None, None
    for kt in times:
        kts = str(kt)
        item = d[kts]
        v = item["vector"] if isinstance(item, dict) and "vector" in item else None
        if v is None: continue
        if kt <= t:
            before_t, before_v = kt, v
        if kt >= t:
            after_t, after_v = kt, v
            break
    if before_v is None:
        return after_v
    if after_v is None or after_t == before_t:
        return before_v
    ratio = (t - before_t) / (after_t - before_t)
    return [before_v[0] + (after_v[0] - before_v[0]) * ratio,
            before_v[1] + (after_v[1] - before_v[1]) * ratio,
            before_v[2] + (after_v[2] - before_v[2]) * ratio]


def combine_keyframes(body_val, torso_val):
    """Combine body and torso keyframe data by adding values."""
    if isinstance(body_val, dict) and isinstance(torso_val, dict):
        result = {}
        # Collect all time points
        all_times = set()
        if not _is_vector_dict(body_val):
            all_times |= set(float(k) for k in body_val.keys())
        if not _is_vector_dict(torso_val):
            all_times |= set(float(k) for k in torso_val.keys())
        all_times = sorted(all_times)
        if all_times:
            for t in all_times:
                bv = _get_vector_at_time(body_val, t)
                tv = _get_vector_at_time(torso_val, t)
                if bv is not None and tv is not None:
                    result[str(t)] = {"vector": add_vectors(bv, tv)}
                elif bv is not None:
                    result[str(t)] = {"vector": list(bv)}
                else:
                    result[str(t)] = {"vector": list(tv)}
            return result
        # Both are simple vector dicts
        bv = body_val["vector"] if _is_vector_dict(body_val) else [0, 0, 0]
        tv = torso_val["vector"] if _is_vector_dict(torso_val) else [0, 0, 0]
        return {"vector": add_vectors(bv, tv)}

    # At least one isn't a dict - just use whichever one is
    return torso_val if torso_val is not None else body_val


def remap_bones(bones: dict) -> dict:
    """Simple bone name remapping without merging."""
    result = {}
    for bone_name, channels in bones.items():
        geo_name = BONE_MAP.get(bone_name)
        if geo_name is None:
            print(f"  WARNING: Unknown bone '{bone_name}', skipping")
            continue
        if geo_name in result:
            result[geo_name].update(channels)
        else:
            result[geo_name] = channels
    return result


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
            if "rotation" in merged:
                merged["rotation"] = combine_keyframes(merged["rotation"], torso_data["rotation"])
            else:
                merged["rotation"] = torso_data["rotation"]
        if "position" in torso_data:
            if "position" in merged:
                merged["position"] = combine_keyframes(merged["position"], torso_data["position"])
            else:
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
                anim_data["bones"] = remap_bones(anim_data["bones"])
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
