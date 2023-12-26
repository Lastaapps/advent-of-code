use std::{fs, vec};

type Point = (usize, usize);
struct Input {
    start: Point,
    map: Map,
}

struct Map {
    size: usize,
    data: Vec<bool>,
}

impl Map {
    fn new(size: usize) -> Map {
        let data = vec![false; size * size];
        Map { size, data }
    }
    fn set(&mut self, pos: Point, val: bool) {
        self.data[pos.0 * self.size + pos.1] = val
    }
    fn get(&self, pos: Point) -> bool {
        self.data[pos.0 * self.size + pos.1]
    }
}

fn read_map(file_path: &str) -> Input {
    let data = fs::read_to_string(file_path).unwrap();
    let size = data.lines().next().unwrap().len();

    let mut map = Map::new(size);
    let mut start: Option<Point> = None;

    data.lines().enumerate().for_each(|(y, line)| {
        line.chars().enumerate().for_each(|(x, char)| match char {
            '.' => map.set((x, y), true),
            'S' => {
                start = Some((x, y));
                map.set((x, y), true);
            }
            _ => {}
        })
    });

    Input {
        start: start.unwrap(),
        map,
    }
}

struct PathTracker {
    size: usize,
    data: Vec<usize>,
    max_steps: usize,
    ends_cnt: usize,
}

impl PathTracker {
    fn new(size: usize, max_steps: usize) -> PathTracker {
        let data = vec![usize::MAX; size * size];
        PathTracker {
            size,
            data,
            max_steps,
            ends_cnt: 0,
        }
    }

    fn visit(&mut self, point: &Point, distance: usize) {
        if distance % 2 == self.max_steps % 2 && distance <= self.max_steps {
            self.ends_cnt += 1;
        }

        self.data[point.0 * self.size + point.1] = distance;
    }

    fn distance(&self, point: &Point) -> usize {
        self.data[point.0 * self.size + point.1]
    }

    #[allow(dead_code)]
    fn print(&self) {
        for j in 0..self.size {
            for i in 0..self.size {
                let value = self.distance(&(i, j));
                if value == usize::MAX {
                    print!("    ");
                } else {
                    print!("{:3} ", value);
                }
            }
            println!()
        }
    }
}

fn neighbours(point: &Point, size: usize) -> Vec<Point> {
    [(usize::MAX, 0), (1, 0), (0, usize::MAX), (0, 1)]
        .iter()
        .map(|offset| {
            (
                offset.0.wrapping_add(point.0),
                offset.1.wrapping_add(point.1),
            )
        })
        .filter(|point| point.0 < size && point.1 < size)
        .collect()
}

fn walk_steps(starts: Vec<(usize, Point)>, map: &Map, steps: usize) -> PathTracker {
    let base_dist = starts.iter().map(|x| x.0).min().unwrap();

    let size = map.size;
    let mut dist = base_dist;
    let mut queue_now = vec![];
    let mut queue_next = vec![];
    let mut tracker = PathTracker::new(size, steps);

    fn insert(
        tracker: &mut PathTracker,
        map: &Map,
        neighbour: Point,
        dist: usize,
        queue: &mut Vec<Point>,
    ) {
        if tracker.distance(&neighbour) != usize::MAX {
            return;
        }
        if !map.get(neighbour) {
            return;
        }

        tracker.visit(&neighbour, dist);
        queue.push(neighbour);
    }

    let mut starts = starts;
    starts.sort_by(|a, b| b.0.cmp(&a.0));

    while dist <= base_dist + 2 * size {
        while starts.last().map(|x| x.0 == dist).unwrap_or(false) {
            let start = starts.remove(starts.len() - 1);
            insert(&mut tracker, map, start.1, dist, &mut queue_now)
        }

        dist += 1;

        queue_now.iter().for_each(|point| {
            neighbours(point, size)
                .into_iter()
                .for_each(|neighbour| insert(&mut tracker, map, neighbour, dist, &mut queue_next))
        });

        (queue_now, queue_next) = (queue_next, queue_now);
        queue_next.clear()
    }

    tracker
}

fn part01(file_path: &str, steps: usize) -> usize {
    let input = read_map(file_path);
    let root = walk_steps(vec![(0, input.start)], &input.map, steps);
    root.ends_cnt
}

struct Part02Cache {
    filled_root: usize,    // filled main piece
    filled_inverse: usize, // filled neighbours of the roots
}
const USE_CACHE: bool = true;

/**
 * @param steps_base - steps in the corresponding corner of the root frame
 */
fn resolve_corner(
    map: &Map,
    start: Point,
    steps_base: usize,
    steps_target: usize,
    cache: &Part02Cache,
) -> usize {
    let mut gardens = 0;

    if steps_base + 2 > steps_target {
        return gardens;
    }

    let steps_base = steps_base + 2;
    let steps = steps_target - steps_base;

    // number of inner fields plus one border region
    let cnt = steps / map.size;

    // filled fields (two version because maps are odd, not even)
    gardens += {
        // closer fields
        let res = if !USE_CACHE {
            let base = steps_base;
            walk_steps(vec![(base, start)], map, steps_target).ends_cnt
        } else {
            cache.filled_root
        };
        (cnt / 2).pow(2) * res
    };
    gardens += if cnt != 0 {
        // further fields
        let res = if !USE_CACHE {
            let base = steps_base + map.size;
            walk_steps(vec![(base, start)], map, steps_target).ends_cnt
        } else {
            cache.filled_inverse
        };
        (((cnt - 1) / 2).pow(2) + ((cnt - 1) / 2)) * res
    } else {
        0
    };

    gardens += if cnt != 0 {
        // border inner
        let base = steps_base + map.size * (cnt - 1);
        let res = walk_steps(vec![(base, start)], map, steps_target).ends_cnt;
        cnt * res
    } else {
        0
    };
    gardens += {
        // border outer
        let base = steps_base + map.size * cnt;
        let res = walk_steps(vec![(base, start)], map, steps_target).ends_cnt;
        (cnt + 1) * res
    };

    return gardens;
}

fn resolve_horizontals(
    map: &Map,
    root: &PathTracker,
    starts: &Vec<Point>,
    steps: usize,
    cache: &Part02Cache,
) -> usize {
    let mut gardens = 0;
    let size = map.size;

    let ends: Vec<_> = if starts.first().unwrap().0 == starts.last().unwrap().0 {
        starts.iter().map(|x| (size - 1 - x.0, x.1)).collect()
    } else {
        starts.iter().map(|x| (x.0, size - 1 - x.1)).collect()
    };
    let mut prev: Vec<_> = { ends.iter().map(|point| root.distance(&point)).collect() };

    // wait for differences to settle, will waste resources for small step numbers
    // prev = root
    // while true (not optimal)
    //   create starts with distances from prev to current
    //   run current
    //   if all current.dist - prev.dist == size
    //     create mask
    //     break
    //   record prev
    //   prev = current
    let walked: Vec<_> = loop {
        let starts_complete: Vec<(usize, Point)> = prev
            .iter()
            .map(|x| x + 1)
            .zip(starts.iter().map(|x| *x))
            .collect();

        let current_tracker = walk_steps(starts_complete, map, steps);
        let current: Vec<_> = ends
            .iter()
            .map(|point| current_tracker.distance(&point))
            .collect();

        gardens += current_tracker.ends_cnt;

        if current.iter().zip(prev.iter()).all(|(c, p)| c - p == size) {
            break current;
        }

        prev = current;
    }
    .into_iter()
    .map(|x| x + 1)
    .collect();

    let min = *walked.iter().min().unwrap();
    if min >= steps {
        return gardens;
    }
    let rem_steps = steps - min;
    let cnt = rem_steps / size;

    let starts_with_root_like = {
        let point = starts.first().unwrap();
        (walked.first().unwrap() - root.distance(point)) / size % 2 != 0
    };

    gardens += {
        // closer fields
        let res = if !USE_CACHE {
            let starts_complete: Vec<(usize, Point)> = walked
                .iter()
                .map(|x| *x)
                .zip(starts.iter().map(|x| *x))
                .collect();
            walk_steps(starts_complete, map, steps).ends_cnt
        } else {
            if starts_with_root_like {
                cache.filled_root
            } else {
                cache.filled_inverse
            }
        };
        cnt / 2 * res
    };
    gardens += if cnt != 0 {
        // further fields
        let res = if !USE_CACHE {
            let starts_complete: Vec<(usize, Point)> = walked
                .iter()
                .map(|x| x + size)
                .zip(starts.iter().map(|x| *x))
                .collect();
            walk_steps(starts_complete, map, steps).ends_cnt
        } else {
            if starts_with_root_like {
                cache.filled_inverse
            } else {
                cache.filled_root
            }
        };
        (cnt - 1) / 2 * res
    } else {
        0
    };

    gardens += if cnt != 0 {
        // border inner
        let starts_complete: Vec<(usize, Point)> = walked
            .iter()
            .map(|x| x + size * (cnt - 1))
            .zip(starts.iter().map(|x| *x))
            .collect();
        let res = walk_steps(starts_complete, map, steps).ends_cnt;
        res
    } else {
        0
    };
    gardens += {
        // border outer
        let starts_complete: Vec<(usize, Point)> = walked
            .iter()
            .map(|x| x + size * cnt)
            .zip(starts.iter().map(|x| *x))
            .collect();
        let res = walk_steps(starts_complete, map, steps).ends_cnt;
        res
    };

    gardens
}

fn part02(file_path: &str, steps: usize) -> usize {
    let Input { start, map } = read_map(file_path);
    let size = map.size;

    let mut gardens = 0;
    let root = walk_steps(vec![(0, start)], &map, steps);
    let inverse = walk_steps(vec![(1, start)], &map, steps);
    gardens += root.ends_cnt;

    // caches results for filled tiles
    let cache = Part02Cache {
        // values are invalid in case steps don't fill the root tile
        // in that case other tiles are also not filled, so no one cares
        filled_root: root.ends_cnt,
        filled_inverse: inverse.ends_cnt,
    };

    let corners = [(0, 0), (0, size - 1), (size - 1, 0), (size - 1, size - 1)];
    gardens += corners
        .into_iter()
        .map(|corner| {
            let start = (size - 1 - corner.0, size - 1 - corner.1);
            resolve_corner(&map, start, root.distance(&corner), steps, &cache)
        })
        .sum::<usize>();

    let horizontals: [Vec<Point>; 4] = [
        (0..size).into_iter().map(|x| (x, 0)).collect(),
        (0..size).into_iter().map(|x| (x, size - 1)).collect(),
        (0..size).into_iter().map(|y| (0, y)).collect(),
        (0..size).into_iter().map(|y| (size - 1, y)).collect(),
    ];
    gardens += horizontals
        .into_iter()
        .map(|starts| resolve_horizontals(&map, &root, &starts, steps, &cache))
        .sum::<usize>();

    return gardens;
}

fn main() {
    assert_eq!(part01("./input_test.txt", 6), 16);
    let part01_res = part01("./input_prod.txt", 64);
    println!("Part 01: {}", part01_res);
    assert_eq!(part01_res, 3814);

    assert_eq!(part02("./input_test.txt", 6), 16);
    assert_eq!(part02("./input_test.txt", 10), 50);
    assert_eq!(part02("./input_test.txt", 50), 1594);
    assert_eq!(part02("./input_test.txt", 100), 6536);
    assert_eq!(part02("./input_test.txt", 500), 167004);
    assert_eq!(part02("./input_test.txt", 1000), 668697);
    assert_eq!(part02("./input_test.txt", 5000), 16733044);
    let part02_res = part02("./input_prod.txt", 26501365);
    println!("Part 02: {}", part02_res);
    assert_eq!(part02_res, 632257949158206);
}
