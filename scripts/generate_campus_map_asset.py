#!/usr/bin/env python3
"""把 OSM 校园范围数据清洗为 App 可直接读取的 GeoJSON。"""

from __future__ import annotations

import argparse
import json
import xml.etree.ElementTree as ET
from dataclasses import dataclass
from pathlib import Path


@dataclass(frozen=True)
class OsmWay:
    osm_id: str
    node_ids: tuple[str, ...]
    tags: dict[str, str]


@dataclass(frozen=True)
class CampusSource:
    campus: str
    path: Path


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--yuntang", type=Path, required=True)
    parser.add_argument("--jinpenling", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    return parser.parse_args()


def parse_osm(path: Path) -> tuple[dict[str, tuple[float, float]], list[OsmWay]]:
    root = ET.parse(path).getroot()
    nodes = {
        element.attrib["id"]: (
            float(element.attrib["lon"]),
            float(element.attrib["lat"]),
        )
        for element in root.findall("node")
    }
    ways: list[OsmWay] = []
    for element in root.findall("way"):
        ways.append(
            OsmWay(
                osm_id=element.attrib["id"],
                node_ids=tuple(node.attrib["ref"] for node in element.findall("nd")),
                tags={
                    tag.attrib["k"]: tag.attrib["v"]
                    for tag in element.findall("tag")
                },
            )
        )
    return nodes, ways


def coordinates_for_way(
    way: OsmWay,
    nodes: dict[str, tuple[float, float]],
) -> list[tuple[float, float]]:
    return [nodes[node_id] for node_id in way.node_ids if node_id in nodes]


def polygon_area(coordinates: list[tuple[float, float]]) -> float:
    if len(coordinates) < 3:
        return 0.0
    area = 0.0
    previous = coordinates[-1]
    for current in coordinates:
        area += previous[0] * current[1] - current[0] * previous[1]
        previous = current
    return abs(area) / 2.0


def find_campus_boundary(
    campus: str,
    nodes: dict[str, tuple[float, float]],
    ways: list[OsmWay],
) -> tuple[OsmWay, list[tuple[float, float]]]:
    candidates: list[tuple[float, OsmWay, list[tuple[float, float]]]] = []
    for way in ways:
        if way.tags.get("amenity") != "university":
            continue
        coordinates = coordinates_for_way(way, nodes)
        if len(coordinates) < 4 or coordinates[0] != coordinates[-1]:
            continue
        candidates.append((polygon_area(coordinates), way, coordinates))
    if not candidates:
        raise RuntimeError(f"{campus}: 未找到 amenity=university 的闭合校园边界")
    _, boundary_way, boundary = max(candidates, key=lambda candidate: candidate[0])
    return boundary_way, boundary


def point_in_polygon(
    point: tuple[float, float],
    polygon: list[tuple[float, float]],
) -> bool:
    x, y = point
    inside = False
    previous = polygon[-1]
    for current in polygon:
        crosses = (current[1] > y) != (previous[1] > y)
        if crosses:
            intersection_x = (
                (previous[0] - current[0])
                * (y - current[1])
                / (previous[1] - current[1])
                + current[0]
            )
            if x < intersection_x:
                inside = not inside
        previous = current
    return inside


def polygon_center(coordinates: list[tuple[float, float]]) -> tuple[float, float]:
    points = coordinates[:-1] if coordinates[0] == coordinates[-1] else coordinates
    return (
        sum(point[0] for point in points) / len(points),
        sum(point[1] for point in points) / len(points),
    )


def render_kind(tags: dict[str, str]) -> str | None:
    leisure = tags.get("leisure", "")
    landuse = tags.get("landuse", "")
    natural = tags.get("natural", "")
    if (
        natural == "water"
        or "water" in tags
        or landuse in {"reservoir", "basin"}
    ):
        return "water"
    if (
        leisure in {"pitch", "track", "stadium", "swimming_pool"}
        or "sport" in tags
    ):
        return "sport"
    if (
        leisure in {"park", "garden"}
        or landuse in {"grass", "meadow", "recreation_ground", "village_green"}
        or natural in {"grassland", "wood"}
    ):
        return "green"
    if tags.get("building") not in {None, "", "no"}:
        return "building"
    return None


def road_kind(tags: dict[str, str]) -> str | None:
    if tags.get("area") == "yes":
        return None
    highway = tags.get("highway", "")
    if highway in {"residential", "unclassified", "tertiary", "secondary"}:
        return "main"
    if highway in {"service", "living_street"}:
        return "service"
    if highway in {"pedestrian", "footway", "path", "cycleway", "steps"}:
        return "footway"
    return None


def road_width(tags: dict[str, str], kind: str) -> float:
    raw_width = tags.get("width", "").lower().replace("m", "").strip()
    try:
        return max(1.0, min(float(raw_width), 12.0))
    except ValueError:
        return {"main": 7.0, "service": 4.5, "footway": 2.4}[kind]


def category_for(name: str, kind: str) -> str:
    if kind == "water":
        return "水域"
    if kind == "green":
        return "绿地"
    if kind == "sport":
        return "体育"
    if "图书" in name:
        return "图书馆"
    if any(word in name for word in ("体育", "游泳馆", "健身")):
        return "体育"
    if any(word in name for word in ("食堂", "餐厅", "甘怡园")):
        return "食堂"
    if any(word in name for word in ("宿舍", "公寓", "至诚", "弘毅", "行健", "敏行")):
        return "宿舍"
    if any(word in name for word in ("行政", "办公", "大会堂")):
        return "行政办公"
    if any(
        word in name
        for word in ("教学", "实验", "工科", "理科", "文科", "工程训练", "艺术楼")
    ):
        return "教学楼"
    if any(word in name for word in ("活动中心", "交流中心", "幼儿园")):
        return "生活休闲"
    return "其他"


def feature_from_way(
    campus: str,
    way: OsmWay,
    coordinates: list[tuple[float, float]],
    kind: str,
) -> dict:
    name = way.tags.get("name", "").strip()
    return {
        "type": "Feature",
        "properties": {
            "id": f"osm-way-{way.osm_id}",
            "name": name,
            "campus": campus,
            "category": category_for(name, kind),
            "kind": kind,
        },
        "geometry": {
            "type": "Polygon",
            "coordinates": [[list(point) for point in coordinates]],
        },
    }


def road_from_way(
    campus: str,
    way: OsmWay,
    coordinates: list[tuple[float, float]],
    kind: str,
) -> dict:
    return {
        "properties": {
            "id": f"osm-road-{way.osm_id}",
            "name": way.tags.get("name", "").strip(),
            "campus": campus,
            "kind": kind,
            "width": road_width(way.tags, kind),
        },
        "geometry": {
            "type": "LineString",
            "coordinates": [list(point) for point in coordinates],
        },
    }


def extract_campus(
    source: CampusSource,
) -> tuple[list[dict], list[dict], dict[str, int], str]:
    nodes, ways = parse_osm(source.path)
    boundary_way, boundary = find_campus_boundary(source.campus, nodes, ways)
    features: list[dict] = [
        {
            "type": "Feature",
            "properties": {
                "id": f"osm-campus-boundary-{boundary_way.osm_id}",
                "name": "",
                "campus": source.campus,
                "category": "其他",
                "kind": "boundary",
            },
            "geometry": {
                "type": "Polygon",
                "coordinates": [[list(point) for point in boundary]],
            },
        }
    ]
    roads: list[dict] = []
    counts: dict[str, int] = {"boundary": 1}
    for way in ways:
        if way.osm_id == boundary_way.osm_id:
            continue
        coordinates = coordinates_for_way(way, nodes)
        road_type = road_kind(way.tags)
        if road_type is not None and len(coordinates) >= 2:
            inside_points = sum(point_in_polygon(point, boundary) for point in coordinates)
            if inside_points * 2 >= len(coordinates):
                roads.append(road_from_way(source.campus, way, coordinates, road_type))
        if len(coordinates) < 4 or coordinates[0] != coordinates[-1]:
            continue
        kind = render_kind(way.tags)
        if kind is None or not point_in_polygon(polygon_center(coordinates), boundary):
            continue
        features.append(feature_from_way(source.campus, way, coordinates, kind))
        counts[kind] = counts.get(kind, 0) + 1
    boundary_name = boundary_way.tags.get("name", "未命名校园边界")
    return features, roads, counts, boundary_name


def main() -> None:
    args = parse_args()
    sources = (
        CampusSource("云塘", args.yuntang),
        CampusSource("金盆岭", args.jinpenling),
    )
    all_features: list[dict] = []
    all_roads: list[dict] = []
    for source in sources:
        features, roads, counts, boundary_name = extract_campus(source)
        all_features.extend(features)
        all_roads.extend(roads)
        named_count = sum(bool(feature["properties"]["name"]) for feature in features)
        print(
            f"{source.campus}: boundary={boundary_name}, features={len(features)}, "
            f"named={named_count}, roads={len(roads)}, kinds={counts}"
        )

    kind_order = {"boundary": 0, "green": 1, "water": 2, "sport": 3, "building": 4}
    all_features.sort(
        key=lambda feature: (
            feature["properties"]["campus"],
            kind_order[feature["properties"]["kind"]],
            feature["properties"]["name"],
            feature["properties"]["id"],
        )
    )
    all_roads.sort(
        key=lambda road: (
            road["properties"]["campus"],
            road["properties"]["kind"],
            road["properties"]["id"],
        )
    )
    output = {
        "type": "FeatureCollection",
        "attribution": "© OpenStreetMap contributors",
        "license": "ODbL-1.0",
        "features": all_features,
        "roads": all_roads,
    }
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(
        json.dumps(output, ensure_ascii=False, separators=(",", ":")),
        encoding="utf-8",
    )
    print(f"output={args.output}, bytes={args.output.stat().st_size}")


if __name__ == "__main__":
    main()
